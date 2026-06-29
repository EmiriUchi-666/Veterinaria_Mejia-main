package com.Veterinaria.Mejia.dto;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class VentaRequestDTO {
    private String clienteDni;
    private String clienteNumDoc;
    private String clienteTipoDoc;
    private String clienteNombre;
    private String tipoComprobante; // "Boleta", "Factura"
    private String tipoPago; // "Efectivo", "Yape", etc.
    private BigDecimal total;
    private List<ItemCarritoDTO> items;
}