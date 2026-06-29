package com.Veterinaria.Mejia.facturacion;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.Veterinaria.Mejia.facturacion.NubefactItemDTO;
import com.Veterinaria.Mejia.facturacion.NubefactRequestDTO;
import com.Veterinaria.Mejia.models.ComprobanteElectronico;
import com.Veterinaria.Mejia.models.DetalleComprobante;

/**
 * Componente responsable de mapear/transformar un ComprobanteElectronico
 * de nuestro dominio al DTO (NubefactRequestDTO) que espera la API de Nubefact.
 */
@Component
public class NubefactMapper {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public NubefactRequestDTO toNubefactRequest(ComprobanteElectronico comp) {
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

        // Mapeo de Items
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
}