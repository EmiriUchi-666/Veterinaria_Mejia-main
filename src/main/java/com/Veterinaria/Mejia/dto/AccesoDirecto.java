package com.Veterinaria.Mejia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO para representar un acceso directo en el dashboard.
 */
@Data
@AllArgsConstructor
public class AccesoDirecto {
    private String rol;
    private String nombre;
    private String url;
    private String icono;
}