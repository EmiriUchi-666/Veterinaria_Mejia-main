package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.Veterinaria.Mejia.clients.NubefactClient;
import com.Veterinaria.Mejia.clients.NubefactClient.NubefactResponseDTO;
import com.Veterinaria.Mejia.models.Cliente;
import com.Veterinaria.Mejia.models.DetalleVenta;
import com.Veterinaria.Mejia.models.FacturacionEstado;
import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.repository.FacturacionEstadoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio especializado en la integración con el proveedor de
 * facturación electrónica (Nubefact). Construye y valida los payloads.
 */
@Service
@RequiredArgsConstructor
public class FacturacionService {
    private static final Logger log = LoggerFactory.getLogger(FacturacionService.class);

    private final NubefactClient nubefactClient;
    private final FacturacionEstadoRepository facturacionEstadoRepository;

    // Inyectamos el valor del IGV desde application.properties, con un valor por defecto de 18.0
    @Value("${nubefact.igv.porcentaje:18.0}")
    private double porcentajeIgv;

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final String TIPO_DOC_RUC = "6";
    private static final String TIPO_DOC_DNI = "1";

    /**
     * Construye el payload (JSON) para enviar a la API de Nubefact a partir de una entidad Venta.
     * Incluye validaciones de negocio y cálculos de impuestos requeridos por SUNAT.
     *
     * @param venta La entidad Venta, que debe incluir su Cliente y la lista de Detalles.
     * @return Un Map que representa la estructura JSON para la API de Nubefact.
     * @throws IllegalArgumentException si los datos del cliente o la venta no son válidos.
     */
    public Map<String, Object> generarPayloadNubefact(Venta venta) {
        // --- 1. VALIDACIONES PREVIAS ---
        validarClienteParaComprobante(venta);

        if (venta.getDetallesVentas() == null || venta.getDetallesVentas().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un ítem.");
        }

        // --- 2. PREPARACIÓN DE DATOS ---
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal totalGravada = BigDecimal.ZERO;

        BigDecimal factorIgv = BigDecimal.valueOf(1 + (porcentajeIgv / 100));

        // --- 3. PROCESAMIENTO DE ITEMS Y CÁLCULO DE TOTALES ---
        for (DetalleVenta detalle : venta.getDetallesVentas()) {
            Map<String, Object> item = new HashMap<>();

            BigDecimal precioUnitarioConIgv = detalle.getPrecioUnitario(); // Precio final que pagó el cliente
            BigDecimal cantidad = detalle.getCantidad();

            // Cálculo inverso para obtener la base imponible (valor unitario)
            BigDecimal valorUnitarioSinIgv = precioUnitarioConIgv.divide(factorIgv, 4, RoundingMode.HALF_UP);
            BigDecimal subtotalSinIgv = valorUnitarioSinIgv.multiply(cantidad);
            BigDecimal igvDelItem = precioUnitarioConIgv.multiply(cantidad).subtract(subtotalSinIgv);

            item.put("unidad_de_medida", detalle.getProducto() != null ? "NIU" : "ZZ");
            item.put("codigo", detalle.getProducto() != null ? detalle.getProducto().getId() : detalle.getServicio().getId());
            item.put("descripcion", detalle.getProducto() != null ? detalle.getProducto().getNombre() : detalle.getServicio().getNombreServicio());
            item.put("cantidad", cantidad);
            item.put("valor_unitario", valorUnitarioSinIgv); // PRECIO SIN IGV
            item.put("precio_unitario", precioUnitarioConIgv); // PRECIO CON IGV 
            item.put("subtotal", subtotalSinIgv);
            item.put("tipo_de_afectacion_igv", 10); // 10: Gravado - Operación Onerosa
            item.put("igv", igvDelItem);
            item.put("total", subtotalSinIgv.add(igvDelItem).setScale(2, RoundingMode.HALF_UP));

            items.add(item);
            totalGravada = totalGravada.add(subtotalSinIgv);
        }

        BigDecimal totalIgv = venta.getTotalVenta().subtract(totalGravada);

        // --- 4. ARMADO DEL PAYLOAD FINAL (CABECERA) ---
        payload.put("operacion", "generar_comprobante");
        payload.put("tipo_de_comprobante", venta.getSerie().startsWith("F") ? 1 : 2);
        payload.put("serie", venta.getSerie());
        payload.put("numero", venta.getCorrelativo());
        payload.put("sunat_transaction", 1); // 1: Venta Interna

        // Datos del Cliente
        Cliente cliente = venta.getCliente();
        payload.put("cliente_tipo_de_documento", "RUC".equals(cliente.getTipoDocumento()) ? TIPO_DOC_RUC : TIPO_DOC_DNI);
        payload.put("cliente_numero_de_documento", cliente.getNumeroDocumento());
        payload.put("cliente_denominacion", cliente.getNombre());
        payload.put("cliente_direccion", cliente.getDireccion() != null ? cliente.getDireccion() : "");
        payload.put("cliente_email", cliente.getEmail() != null ? cliente.getEmail() : "");

        // Fecha y Moneda
        payload.put("fecha_de_emision", venta.getFechaEmision().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        payload.put("moneda", 1); // 1: Soles (PEN)

        // Totales
        payload.put("porcentaje_de_igv", porcentajeIgv);
        payload.put("total_gravada", totalGravada.setScale(2, RoundingMode.HALF_UP));
        payload.put("total_igv", totalIgv.setScale(2, RoundingMode.HALF_UP));
        payload.put("total", venta.getTotalVenta());

        // Opciones de envío
        payload.put("enviar_automaticamente_a_la_sunat", true);
        payload.put("enviar_automaticamente_al_cliente", false);

        // Items
        payload.put("items", items);

        return payload;
    }

    /**
     * Valida que los datos del cliente sean consistentes con el tipo de comprobante a emitir.
     *
     * @param venta La entidad Venta a validar.
     * @throws IllegalArgumentException si la validación falla.
     */
    private void validarClienteParaComprobante(Venta venta) {
        if (venta.getCliente() == null || venta.getCliente().getNumeroDocumento() == null) {
            throw new IllegalArgumentException("La venta debe estar asociada a un cliente con documento.");
        }

        String numeroDocumento = venta.getCliente().getNumeroDocumento().trim();
        String tipoDocumento = venta.getCliente().getTipoDocumento();

        if (venta.getSerie().startsWith("F")) { // Es una Factura
            if (!"RUC".equals(tipoDocumento)) {
                throw new IllegalArgumentException("Para emitir Factura, el cliente debe tener tipo de documento RUC.");
            }
            if (numeroDocumento.length() != 11) {
                throw new IllegalArgumentException("El RUC del cliente debe tener 11 dígitos para emitir una Factura.");
            }
        } else if (venta.getSerie().startsWith("B")) { // Es una Boleta
            if (!"DNI".equals(tipoDocumento)) {
                throw new IllegalArgumentException("Para emitir Boleta, el cliente debe tener tipo de documento DNI.");
            }
            // Se permite el DNI genérico "00000000" para clientes varios.
            if (numeroDocumento.length() != 8 && !"00000000".equals(numeroDocumento)) {
                throw new IllegalArgumentException("El DNI del cliente debe tener 8 dígitos para emitir una Boleta.");
            }
        } else {
            throw new IllegalArgumentException("La serie del comprobante (" + venta.getSerie() + ") no es válida. Debe empezar con 'F' o 'B'.");
        }
    }

    /**
     * Orquesta el envío del comprobante a Nubefact y actualiza el estado de facturación.
     * @param estadoFacturacion El registro de estado a procesar.
     */
    public void procesarEnvioNubefact(FacturacionEstado estadoFacturacion) {
        Venta venta = estadoFacturacion.getVenta();
        try {
            Map<String, Object> payload = generarPayloadNubefact(venta);
            log.info("[NUBEFACT] Payload generado para la venta #{}: {}", venta.getId(), payload);

            NubefactResponseDTO response = nubefactClient.enviarComprobante(payload);

            if (response.esExitoso()) {
                estadoFacturacion.setEstado(FacturacionEstado.EstadoFacturacion.ACEPTADO);
                estadoFacturacion.setUrlPdf(response.getEnlaceDelPdf());
                estadoFacturacion.setUrlXml(response.getEnlaceDelXml());
                estadoFacturacion.setCodigoHash(response.getCodigoHash());
                estadoFacturacion.setCodigoRespuestaSunat(response.getSunatCodigo());
                estadoFacturacion.setRespuestaApi(response.getSunatDescripcion());
                log.info("[NUBEFACT] Venta #{} ACEPTADA por SUNAT. PDF: {}", venta.getId(), response.getEnlaceDelPdf());
            } else {
                estadoFacturacion.setEstado(FacturacionEstado.EstadoFacturacion.RECHAZADO);
                estadoFacturacion.setRespuestaApi(response.getErrors());
                log.warn("[NUBEFACT] Venta #{} RECHAZADA por SUNAT: {}", venta.getId(), response.getErrors());
            }
            } catch (Exception e) {
                estadoFacturacion.setEstado(FacturacionEstado.EstadoFacturacion.ERROR);
                // Cambia esto:
                estadoFacturacion.setRespuestaApi("Error técnico: " + e.getMessage()); 
                // Por esto (si usas un cliente que lanza HttpClientErrorException):
                if (e instanceof org.springframework.web.client.HttpStatusCodeException) {
                    String responseBody = ((org.springframework.web.client.HttpStatusCodeException) e).getResponseBodyAsString();
                    String mensaje = "Nubefact dice: " + responseBody;
                    // FIX: error de configuración de cuenta, no de código. La serie
                    // (B001/F001 o BBB1/FFF1) tiene que estar habilitada en el panel
                    // de Nubefact para este RUC/entorno (sandbox vs producción).
                    if (responseBody != null && responseBody.toLowerCase().contains("serie")) {
                        mensaje += " — Verifica en tu panel de Nubefact que la serie "
                                + "configurada en application.properties (nubefact.serie.boleta / "
                                + "nubefact.serie.factura) esté habilitada para este RUC. En modo "
                                + "sandbox (nubefact.modo.prueba=true) normalmente debes usar las "
                                + "series de demostración que Nubefact te asignó, no series reales.";
                    }
                    estadoFacturacion.setRespuestaApi(mensaje);
                }
                log.error("[NUBEFACT] Error: ", e);
            }
    }

    /**
     * Reintenta el envío de un comprobante que falló previamente.
     * @param estado El estado de facturación a reintentar.
     * @return El estado de facturación actualizado tras el reintento.
     */
    public FacturacionEstado reintentarEnvioNubefact(FacturacionEstado estado) {
        log.info("[NUBEFACT-REINTENTO] Reintentando envío para la venta #{}", estado.getVenta().getId());
        estado.setEstado(FacturacionEstado.EstadoFacturacion.PENDIENTE); // Lo marcamos como pendiente para el nuevo intento
        procesarEnvioNubefact(estado);
        return estado;
    }
}