package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.facturacion.NubefactItemDTO;
import com.Veterinaria.Mejia.facturacion.NubefactRequestDTO;
import com.Veterinaria.Mejia.facturacion.NubefactResponseDTO;
import com.Veterinaria.Mejia.models.ComprobanteElectronico;
import com.Veterinaria.Mejia.models.DetalleComprobante;
import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.repository.ComprobanteElectronicoRepository;
import com.Veterinaria.Mejia.repository.DetalleVentaRepository;
import com.Veterinaria.Mejia.repository.UsuarioRepository;
import com.Veterinaria.Mejia.repository.VentaRepository;

import tools.jackson.databind.ObjectMapper;

/**
 * Servicio de Facturación Electrónica — integración con PSE Nubefact.
 *
 * Flujo:
 *  1. Genera el comprobante en la BD (serie+correlativo auto)
 *  2. Arma el JSON para Nubefact
 *  3. Envía vía HTTP POST a la API de Nubefact
 *  4. Actualiza el estado con la respuesta SUNAT (CDR, URL PDF)
 *
 * Configurar en application.properties:
 *   nubefact.api.url=https://api.nubefact.com/api/v1/{ruc}
 *   nubefact.api.token=TU_TOKEN_AQUI
 *   nubefact.ruc=20600000001
 *   nubefact.razon.social=VETERINARIA MEJIA E.I.R.L.
 */
@Service
public class FacturacionElectronicaService {

    @Value("${nubefact.api.url:https://api.nubefact.com/api/v1}")
    private String apiUrl;

    @Value("${nubefact.api.token:DEMO}")
    private String apiToken;

    @Value("${nubefact.ruc:20600000001}")
    private String rucEmisor;

    @Value("${nubefact.razon.social:VETERINARIA MEJIA E.I.R.L.}")
    private String razonSocialEmisor;

    @Value("${nubefact.modo.prueba:true}")
    private boolean modoPrueba;

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Autowired private ComprobanteElectronicoRepository comprobanteRepo;
    @Autowired private DetalleVentaRepository detalleVentaRepo;
    @Autowired private VentaRepository ventaRepo;
    @Autowired private UsuarioRepository usuarioRepo;

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
        var detallesVenta = detalleVentaRepo.findAll().stream()
                .filter(d -> d.getVenta() != null && d.getVenta().getId().equals(venta.getId()))
                .toList();

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
            NubefactRequestDTO req = construirRequest(comp);
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(req);

            String url = apiUrl + "/" + rucEmisor + "/comprobante";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Token " + apiToken)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                NubefactResponseDTO resp = mapper.readValue(response.body(), NubefactResponseDTO.class);
                comp.setCodigoRespuestaSunat(resp.sunatCodigo);
                comp.setDescripcionRespuesta(resp.sunatDescripcion);
                comp.setCodigoHash(resp.codigoHash);
                comp.setUrlPdf(resp.enlaceDelPdf);
                comp.setUrlXml(resp.enlaceDelXml);
                comp.setEstado(resp.esExitoso()
                        ? ComprobanteElectronico.EstadoComprobante.ACEPTADO
                        : ComprobanteElectronico.EstadoComprobante.RECHAZADO);
            } else {
                comp.setEstado(ComprobanteElectronico.EstadoComprobante.ERROR);
                comp.setDescripcionRespuesta("HTTP " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            comp.setEstado(ComprobanteElectronico.EstadoComprobante.ERROR);
            comp.setDescripcionRespuesta("Error de conexión con Nubefact: " + e.getMessage());
        }
        comprobanteRepo.save(comp);
    }

    private NubefactRequestDTO construirRequest(ComprobanteElectronico comp) {
        NubefactRequestDTO req = new NubefactRequestDTO();
        req.tipoDeComprobante = Integer.parseInt(comp.getTipoComprobante().codigoSunat);
        req.serie = comp.getSerie();
        req.numero = comp.getNumero();
        req.clienteTipoDeDocumento = Integer.parseInt(comp.getReceptorTipoDoc());
        req.clienteNumeroDeDocumento = comp.getReceptorNumDoc();
        req.clienteDenominacion = comp.getReceptorDenominacion();
        req.clienteEmail = comp.getReceptorEmail() != null ? comp.getReceptorEmail() : "";
        req.fechaDeEmision = comp.getFechaEmision().format(DATE_FMT);
        req.totalGravada = comp.getTotalGravada();
        req.totalIgv = comp.getTotalIgv();
        req.total = comp.getTotal();
        req.medioDePago = comp.getMedioPago();
        req.enviarCliente = comp.getReceptorEmail() != null && !comp.getReceptorEmail().isBlank();

        // Items
        List<NubefactItemDTO> items = new ArrayList<>();
        if (comp.getDetalles() != null) {
            for (DetalleComprobante d : comp.getDetalles()) {
                NubefactItemDTO item = new NubefactItemDTO();
                item.unidadDeMedida = d.getUnidadMedida();
                item.codigo = d.getCodigo() != null ? d.getCodigo() : "";
                item.descripcion = d.getDescripcion();
                item.cantidad = d.getCantidad();
                item.valorUnitario = d.getValorUnitario();
                item.precioUnitario = d.getPrecioUnitario();
                item.subtotal = d.getValorUnitario().multiply(d.getCantidad()).setScale(2, RoundingMode.HALF_UP);
                item.igv = d.getIgv();
                item.total = d.getTotal();
                items.add(item);
            }
        }
        req.items = items;
        return req;
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
}
