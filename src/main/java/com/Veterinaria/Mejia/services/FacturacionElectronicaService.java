package com.Veterinaria.Mejia.services;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.facturacion.NubefactClient;
import com.Veterinaria.Mejia.facturacion.NubefactMapper;
import com.Veterinaria.Mejia.facturacion.NubefactRequestDTO;
import com.Veterinaria.Mejia.facturacion.NubefactResponseDTO;
import com.Veterinaria.Mejia.models.ComprobanteElectronico;
import com.Veterinaria.Mejia.models.DetalleComprobante;
import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.repository.ComprobanteElectronicoRepository;
import com.Veterinaria.Mejia.repository.DetalleVentaRepository;
import com.Veterinaria.Mejia.repository.UsuarioRepository;
import com.Veterinaria.Mejia.repository.VentaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FacturacionElectronicaService {

    @Value("${nubefact.api.token:DEMO}")
    private String apiToken;

    @Value("${nubefact.modo.prueba:true}")
    private boolean modoPrueba;

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    private final ComprobanteElectronicoRepository comprobanteRepo;
    private final DetalleVentaRepository detalleVentaRepo;
    private final VentaRepository ventaRepo;
    private final UsuarioRepository usuarioRepo;
    private final NubefactMapper nubefactMapper;
    private final NubefactClient nubefactClient;
    private final EmailService emailService; // Inyectamos el nuevo servicio

    /**
     * Emite una boleta de venta electrónica para la venta indicada.
     *
     * @param ventaId        ID de la venta origen
     * @param dniReceptor    DNI del cliente (puede ser "00000000" para consumidor final)
     * @param nombreReceptor Nombre del cliente
     * @param emailReceptor  Email (opcional, para envío del PDF)
     * @param usuarioLogin   Login del cajero que emite
     * @param medioPago      EFECTIVO, YAPE, TARJETA, PLIN
     */
    @Transactional
    public ComprobanteElectronico emitirBoleta(Integer ventaId, String dniReceptor,
            String nombreReceptor, String emailReceptor, String usuarioLogin, String medioPago) {
        return emitirComprobante(ventaId, ComprobanteElectronico.TipoComprobante.BOLETA,
                dniReceptor, "1", nombreReceptor, emailReceptor, usuarioLogin, medioPago);
    }

    /**
     * Emite una factura electrónica para la venta indicada.
     * La factura requiere RUC del receptor (tipo_doc = "6").
     */
    @Transactional
    public ComprobanteElectronico emitirFactura(Integer ventaId, String rucReceptor,
            String razonSocialReceptor, String emailReceptor, String usuarioLogin, String medioPago) {
        return emitirComprobante(ventaId, ComprobanteElectronico.TipoComprobante.FACTURA,
                rucReceptor, "6", razonSocialReceptor, emailReceptor, usuarioLogin, medioPago);
    }

    // ─── LÓGICA CENTRAL ──────────────────────────────────────────────────────

    private ComprobanteElectronico emitirComprobante(Integer ventaId, ComprobanteElectronico.TipoComprobante tipo,
            String numDoc, String tipoDoc, String denominacion, String email,
            String usuarioLogin, String medioPago) {

        // 1. Obtener venta
        Venta venta = ventaRepo.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + ventaId));

        // 2. Generar correlativo
        String serie = tipo.serieDefecto;
        int siguiente = comprobanteRepo.findMaxNumeroBySerie(serie).orElse(0) + 1;

        // 3. Construir comprobante en BD
        ComprobanteElectronico comp = new ComprobanteElectronico();
        comp.setTipoComprobante(tipo);
        comp.setSerie(serie);
        comp.setNumero(siguiente);
        comp.setFechaEmision(LocalDate.now());
        comp.setReceptorTipoDoc(tipoDoc);
        comp.setReceptorNumDoc(numDoc != null && !numDoc.isBlank() ? numDoc : "00000000");
        comp.setReceptorDenominacion(denominacion != null && !denominacion.isBlank() ? denominacion : "CONSUMIDOR FINAL");
        comp.setReceptorEmail(email);
        comp.setVenta(venta);
        comp.setMedioPago(medioPago != null ? medioPago.toUpperCase() : "EFECTIVO");
        comp.setEstado(ComprobanteElectronico.EstadoComprobante.PENDIENTE);

        usuarioRepo.findByNombreUsuario(usuarioLogin).ifPresent(comp::setUsuario);

        // 4. Calcular totales (precio con IGV → desagregar base + IGV)
        BigDecimal totalConIgv = venta.getTotalVenta();
        BigDecimal baseImponible = totalConIgv.divide(BigDecimal.ONE.add(IGV_RATE), 2, RoundingMode.HALF_UP);
        BigDecimal igvTotal = totalConIgv.subtract(baseImponible);

        comp.setTotalGravada(baseImponible);
        comp.setTotalIgv(igvTotal);
        comp.setTotal(totalConIgv);

        // 5. Construir detalles desde DetalleVenta
        List<DetalleComprobante> detalles = construirDetalles(comp, venta);
        comp.setDetalles(detalles);

        ComprobanteElectronico guardado = comprobanteRepo.save(comp);

        // 6. Enviar a Nubefact (si hay token configurado)
        if (!"DEMO".equals(apiToken) && !modoPrueba) {
            enviarANubefact(guardado);
        } else {
            // Modo demo: marcar como "enviado localmente"
            guardado.setEstado(ComprobanteElectronico.EstadoComprobante.ENVIADO);
            guardado.setDescripcionRespuesta(
                "MODO DEMO — Configure nubefact.api.token en application.properties para envío real a SUNAT.");
            comprobanteRepo.save(guardado);
        }

        return guardado;
    }

    // ─── CONSTRUCCIÓN DETALLES ────────────────────────────────────────────────

    private List<DetalleComprobante> construirDetalles(ComprobanteElectronico comp, Venta venta) {
        List<DetalleComprobante> detalles = new ArrayList<>();

        // Intentar obtener detalles de la venta
        var detallesVenta = detalleVentaRepo.buscarDetallesPorVentaJPQL(venta.getId());

        if (detallesVenta.isEmpty()) {
            // Si no hay detalles, crear una línea genérica con el total
            detalles.add(crearDetalleGenerico(comp, "Venta veterinaria #" + venta.getId(), BigDecimal.ONE, venta.getTotalVenta()));
        } else {
            for (var dv : detallesVenta) {
                DetalleComprobante dc = new DetalleComprobante();
                dc.setComprobante(comp);
                String desc = "";
                if (dv.getProducto() != null) {
                    desc = dv.getProducto().getNombre();
                    dc.setCodigo(String.valueOf(dv.getProducto().getId()));
                    dc.setUnidadMedida(determinarUnidad(dv.getProducto().getTipoUnidad()));
                } else if (dv.getServicio() != null) {
                    desc = dv.getServicio().getNombreServicio();
                    dc.setCodigo("SVC-" + dv.getServicio().getId());
                }
                dc.setDescripcion(desc.isBlank() ? "Servicio veterinario" : desc);
                BigDecimal cant = dv.getCantidad() != null ? new BigDecimal(dv.getCantidad().toString()) : BigDecimal.ONE;
                BigDecimal precio = dv.getPrecioUnitario() != null ? dv.getPrecioUnitario() : BigDecimal.ZERO;
                dc.setCantidad(cant);
                dc.setPrecioUnitario(precio.setScale(2, RoundingMode.HALF_UP));
                BigDecimal valor = precio.divide(BigDecimal.ONE.add(IGV_RATE), 2, RoundingMode.HALF_UP);
                dc.setValorUnitario(valor);
                BigDecimal subtotal = valor.multiply(cant).setScale(2, RoundingMode.HALF_UP);
                BigDecimal igv = precio.multiply(cant).subtract(subtotal).setScale(2, RoundingMode.HALF_UP);
                dc.setIgv(igv);
                dc.setTotal(precio.multiply(cant).setScale(2, RoundingMode.HALF_UP));
                detalles.add(dc);
            }
        }
        return detalles;
    }

    private DetalleComprobante crearDetalleGenerico(ComprobanteElectronico comp, String desc,
            BigDecimal cant, BigDecimal precioConIgv) {
        DetalleComprobante dc = new DetalleComprobante();
        dc.setComprobante(comp);
        dc.setDescripcion(desc);
        dc.setCantidad(cant);
        dc.setPrecioUnitario(precioConIgv.setScale(2, RoundingMode.HALF_UP));
        BigDecimal valor = precioConIgv.divide(BigDecimal.ONE.add(IGV_RATE), 2, RoundingMode.HALF_UP);
        dc.setValorUnitario(valor);
        dc.setIgv(precioConIgv.subtract(valor).setScale(2, RoundingMode.HALF_UP));
        dc.setTotal(precioConIgv.setScale(2, RoundingMode.HALF_UP));
        return dc;
    }

    private String determinarUnidad(String tipoUnidad) {
        if (tipoUnidad == null) return "ZZ";
        return switch (tipoUnidad.toLowerCase()) {
            case "kg" -> "KGM";
            case "litros" -> "LTR";
            case "blister", "caja" -> "BX";
            default -> "ZZ";
        };
    }

    // ─── ENVÍO A NUBEFACT ────────────────────────────────────────────────────

    private void enviarANubefact(ComprobanteElectronico comp) {
        try {
            // 1. Mapear nuestro comprobante al DTO de Nubefact
            NubefactRequestDTO requestPayload = nubefactMapper.toNubefactRequest(comp);

            // 2. Ajustar el tipo_de_afectacion_igv para cada item en el payload
            // Esto es crucial para evitar el error "Debe indicar el tipo de impuesto en el ITEM"
            for (Map<String, Object> item : requestPayload.getItems()) {
                BigDecimal itemIgv = (BigDecimal) item.get("igv"); // Asumimos que el mapper ya puso el IGV
                if (itemIgv != null && itemIgv.compareTo(BigDecimal.ZERO) > 0) {
                    item.put("tipo_de_afectacion_igv", 10); // 10: Gravado - Operación Onerosa
                } else {
                    item.put("tipo_de_afectacion_igv", 20); // 20: Exonerado - Operación Onerosa (o 30 Inafecto)
                }
            }

            // 2. Enviar la petición a través del cliente HTTP
            NubefactResponseDTO response = nubefactClient.enviarComprobante(requestPayload);

            // 3. Actualizar nuestro comprobante con la respuesta de SUNAT
            comp.setCodigoRespuestaSunat(response.sunatCodigo);
            comp.setDescripcionRespuesta(response.sunatDescripcion);
            comp.setCodigoHash(response.codigoHash);
            comp.setUrlPdf(response.enlaceDelPdf);
            comp.setUrlXml(response.enlaceDelXml);

            if (response.esExitoso()) {
                comp.setEstado(ComprobanteElectronico.EstadoComprobante.ACEPTADO);
                // Si el comprobante fue aceptado y hay un email, enviarlo.
                if (comp.getReceptorEmail() != null && !comp.getReceptorEmail().isBlank() && response.enlaceDelPdf != null) {
                    enviarComprobantePorEmail(comp);
                }
            } else {
                comp.setEstado(ComprobanteElectronico.EstadoComprobante.RECHAZADO);
            }

        } catch (Exception e) {
            // Mejorar el manejo de errores para capturar detalles de la respuesta HTTP
            if (e instanceof org.springframework.web.client.HttpStatusCodeException) {
                String responseBody = ((org.springframework.web.client.HttpStatusCodeException) e).getResponseBodyAsString();
                comp.setDescripcionRespuesta("Error del servidor Nubefact: " + responseBody);
            } else {
            comp.setEstado(ComprobanteElectronico.EstadoComprobante.ERROR);
            comp.setDescripcionRespuesta("Error al procesar con Nubefact: " + e.getMessage());
            }
        }
        comprobanteRepo.save(comp);
    }

    private void enviarComprobantePorEmail(ComprobanteElectronico comp) {
        try (InputStream in = new URL(comp.getUrlPdf()).openStream()) {
            byte[] pdfBytes = in.readAllBytes();
            String nombreArchivo = comp.getNumeroCompleto() + ".pdf";
            String cuerpoEmail = "Estimado(a) " + comp.getReceptorDenominacion() + ",<br><br>Adjuntamos su comprobante electrónico " + comp.getNumeroCompleto() + ".<br><br>Gracias por su preferencia,<br>Veterinaria Mejía.";
            emailService.enviarEmailConAdjunto(comp.getReceptorEmail(), "Comprobante Electrónico: " + comp.getNumeroCompleto(), cuerpoEmail, pdfBytes, nombreArchivo);
        } catch (Exception e) {
            // Loguear el error pero no detener el flujo principal
            System.err.println("Error al descargar o enviar el PDF por correo: " + e.getMessage());
        }
    }

    /** Reintenta el envío de un comprobante que falló. */
    @Transactional
    public ComprobanteElectronico reenviar(Integer comprobanteId) {
        ComprobanteElectronico comp = comprobanteRepo.findById(comprobanteId)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado"));
        comp.setEstado(ComprobanteElectronico.EstadoComprobante.PENDIENTE);
        enviarANubefact(comp);
        return comp;
    }

    /** Obtiene el historial de comprobantes emitidos. */
    public List<ComprobanteElectronico> obtenerHistorial() {
        return comprobanteRepo.findTop50ByOrderByFechaRegistroDesc();
    }

    /**
     * Reenvía un comprobante por correo electrónico a la dirección registrada.
     * @param comprobanteId ID del comprobante a reenviar.
     */
    @Transactional(readOnly = true) // Es de solo lectura porque no modifica el estado del comprobante.
    public void reenviarPorEmail(Integer comprobanteId) {
        ComprobanteElectronico comp = comprobanteRepo.findById(comprobanteId)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado con ID: " + comprobanteId));

        if (comp.getReceptorEmail() == null || comp.getReceptorEmail().isBlank()) {
            throw new IllegalArgumentException("El comprobante no tiene una dirección de correo electrónico registrada.");
        }
        if (comp.getUrlPdf() == null || comp.getUrlPdf().isBlank()) {
            throw new IllegalArgumentException("El comprobante no tiene un PDF generado para poder enviar.");
        }

        enviarComprobantePorEmail(comp);
    }
}
