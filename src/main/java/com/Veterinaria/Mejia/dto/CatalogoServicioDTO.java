package com.Veterinaria.Mejia.dto;

import java.math.BigDecimal;

import com.Veterinaria.Mejia.models.Servicio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO plano para representar un servicio clínico en la tabla del catálogo del Punto de Venta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoServicioDTO {

    private Integer id;
    private String nombre;
    private BigDecimal precio;

    public static CatalogoServicioDTO desde(Servicio s) {
        return CatalogoServicioDTO.builder()
                .id(s.getId())
                .nombre(s.getNombreServicio())
                .precio(s.getPrecioServicio())
                .build();
    }
}
