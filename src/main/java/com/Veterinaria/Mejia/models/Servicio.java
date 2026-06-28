package com.Veterinaria.Mejia.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Solo letras y espacios, máximo 100 caracteres.
    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre del servicio solo debe contener letras")
    @Size(max = 100, message = "El nombre no debe superar los 100 caracteres")
    @Column(name = "nombre_servicio", nullable = false, length = 100)
    private String nombreServicio;

    // Precio a partir de 10.00
    @NotNull(message = "El precio del servicio es obligatorio")
    @DecimalMin(value = "10.00", message = "El precio del servicio debe ser a partir de S/ 10.00")
    @Column(name = "precio_servicio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioServicio;

    // Siempre activo al crearse
    @Column(name = "estado", nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private Boolean estado = true;
}