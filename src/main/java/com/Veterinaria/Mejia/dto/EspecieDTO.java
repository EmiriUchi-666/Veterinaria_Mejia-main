package com.Veterinaria.Mejia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EspecieDTO {
    private Integer id;
    private String nombre;
    private long cantidad;
}