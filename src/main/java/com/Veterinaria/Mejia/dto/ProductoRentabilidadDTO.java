package com.Veterinaria.Mejia.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRentabilidadDTO {
    private String nombreProducto;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private BigDecimal unidadesVendidas;
    private BigDecimal ingresosTotales;
    private BigDecimal costoTotal;
    private BigDecimal utilidadTotal;
}