package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

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
    @Pattern(regexp = "^[uU][0-9]{8}$", message = "El usuario debe empezar con 'U' seguido de 8 dígitos (DNI).")
    @Column(name = "nombre_usuario", unique = true, nullable = false, length = 9)
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
    @Column(name = "pregunta_secreta", nullable = false, length = 150)
    private String preguntaSecreta;

    @NotBlank(message = "La respuesta secreta es obligatoria")
    @Column(name = "respuesta_secreta", nullable = false, length = 255)
    private String respuestaSecreta;
}