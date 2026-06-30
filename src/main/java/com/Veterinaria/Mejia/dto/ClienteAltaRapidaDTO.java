package com.Veterinaria.Mejia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para el alta rápida de un cliente/dueño desde formularios cortos
 * (panel de venta, registro de dueño). Mantiene separados y obligatorios
 * los 3 campos mínimos: nombre, documento y teléfono. Dirección y email
 * son opcionales.
 */
@Data
public class ClienteAltaRapidaDTO {

    @NotBlank(message = "El nombre completo es obligatorio.")
    private String nombre;

    @NotBlank(message = "El DNI/RUC es obligatorio.")
    @Pattern(regexp = "^[0-9]{8,11}$", message = "El documento debe tener 8 (DNI) u 11 (RUC) dígitos.")
    private String numeroDocumento;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Pattern(regexp = "^[0-9+\\-\\s]{6,15}$", message = "Ingrese un teléfono válido.")
    private String telefono;

    private String tipoDocumento = "DNI";

    // Opcionales
    private String email;
    private String direccion;
}
