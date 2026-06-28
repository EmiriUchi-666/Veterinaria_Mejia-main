package com.Veterinaria.Mejia.dto;
import java.math.BigDecimal;

public interface VentasDiaDTO {
    String getFecha(); // Fecha en formato String (ej. "2026-06-06")
    BigDecimal getTotalMonto();
}