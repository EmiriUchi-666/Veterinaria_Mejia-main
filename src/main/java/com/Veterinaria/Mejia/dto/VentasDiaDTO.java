package com.Veterinaria.Mejia.dto;
import java.math.BigDecimal;

public interface VentasDiaDTO {
    Object getFecha(); // Can be String (from native query) or Date (from JPQL)
    BigDecimal getTotalMonto();
}