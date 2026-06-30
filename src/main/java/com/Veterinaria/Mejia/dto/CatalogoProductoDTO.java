package com.Veterinaria.Mejia.dto;

import java.math.BigDecimal;

import com.Veterinaria.Mejia.models.Producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO plano para representar un producto en la tabla del catálogo del Punto de Venta.
 * Evita exponer la entidad JPA directamente (proxies LAZY de categoria/especie).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoProductoDTO {

    private Integer id;
    private String nombre;
    private String categoria;
    private String especie;
    private BigDecimal precioVentaActual;
    private BigDecimal precioPorFraccion;
    private Boolean permiteFraccionamiento;
    private Integer stockCerrado;
    private BigDecimal stockAbierto;
    private String tipoUnidad;

    public static CatalogoProductoDTO desde(Producto p) {
        return CatalogoProductoDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .categoria(p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría")
                .especie(p.getEspecie() != null ? p.getEspecie().getNombreEspecie() : "General")
                .precioVentaActual(p.getPrecioVentaActual())
                .precioPorFraccion(p.getPrecioPorFraccion())
                .permiteFraccionamiento(Boolean.TRUE.equals(p.getPermiteFraccionamiento()))
                .stockCerrado(p.getStockCerrado() != null ? p.getStockCerrado() : 0)
                .stockAbierto(p.getStockAbierto() != null ? p.getStockAbierto() : BigDecimal.ZERO)
                .tipoUnidad(p.getTipoUnidad())
                .build();
    }
}
