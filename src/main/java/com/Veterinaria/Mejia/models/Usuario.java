package com.Veterinaria.Mejia.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nickname: Empieza con U o u, seguido de exactamente 8 números (DNI)
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "El usuario debe tener entre 4 y 20 caracteres alfanuméricos.")
    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 20)
    private String nombreUsuario;

    // Aquí guardaremos el Hash de BCrypt (No la contraseña plana)
    @Column(nullable = false, length = 255)
    private String contrasena;

    @NotNull(message = "El rol es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private Boolean estado = true; // Siempre activo al nacer

    @NotBlank(message = "Debes elegir una pregunta de seguridad")
    @Size(max = 150)
    @Column(name = "pregunta_secreta", nullable = false, length = 150)
    private String preguntaSecreta;

    @Column(name = "respuesta_secreta", nullable = false, length = 255)
    private String respuestaSecreta;
}