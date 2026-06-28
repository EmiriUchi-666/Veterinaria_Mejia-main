package com.Veterinaria.Mejia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaDTO {
    private Integer id;
    private String nombre;
    private long cantidadProductos; // Almacenará el conteo en vivo de la BD
}