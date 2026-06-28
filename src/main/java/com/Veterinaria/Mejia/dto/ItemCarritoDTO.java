package com.Veterinaria.Mejia.dto;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemCarritoDTO {
    private String tipo; // "PRODUCTO" o "SERVICIO"
    private Integer idItem;
    private BigDecimal cantidad;
    private BigDecimal precio;
    private BigDecimal subtotal;
}