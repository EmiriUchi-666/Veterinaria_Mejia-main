package com.Veterinaria.Mejia.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@jakarta.persistence.Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // FIX: el campo se mapeaba a la columna "razon_social", pero la tabla
    // real "clientes" tiene la restricción NOT NULL sobre la columna
    // "nombre" (no sobre "razon_social"). Por eso cada venta fallaba con
    // "Field 'nombre' doesn't have a default value": Hibernate escribía en
    // razon_social y dejaba la columna nombre completamente sin tocar.
    // Revertido para que el campo apunte a la columna que de verdad existe
    // y tiene la restricción.
    @NotBlank(message = "El nombre o razón social es obligatorio")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Pattern(regexp = "^(DNI|RUC)$", message = "El tipo de documento debe ser DNI o RUC")
    @Column(name = "tipo_documento", length = 3)
    private String tipoDocumento;

    @Pattern(regexp = "^[0-9]{8,11}$", message = "El documento debe tener 8 (DNI) u 11 (RUC) dígitos")
    @Column(name = "numero_documento", unique = true, length = 11)
    private String numeroDocumento;

    @Column(length = 15)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String direccion;
}