package com.Veterinaria.Mejia.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nombre de la empresa: Solo letras y espacios, máximo 200 caracteres
    @NotBlank(message = "El nombre de la empresa proveedora es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre de la empresa solo debe contener letras")
    @Size(max = 200, message = "El nombre de la empresa no debe superar los 200 caracteres")
    @Column(name = "nombre_proveedor", nullable = false, length = 200)
    private String nombreProveedor;

    // RUC exacto de 11 dígitos
    @NotBlank(message = "El RUC de la empresa es obligatorio")
    @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener exactamente 11 dígitos numéricos")
    @Column(unique = true, nullable = false, length = 11)
    private String ruc;

    // Teléfono: Solo números, máximo 9 dígitos
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{1,9}$", message = "El teléfono debe contener solo números y no sobrepasar los 9 dígitos")
    @Column(length = 9)
    private String telefono;

    // Nombre de la persona de contacto: Solo letras
    @NotBlank(message = "El nombre del contacto es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre del contacto solo debe contener letras")
    @Size(max = 100, message = "El nombre del contacto no debe superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String contacto;

    @Column(name = "estado", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean estado;
}