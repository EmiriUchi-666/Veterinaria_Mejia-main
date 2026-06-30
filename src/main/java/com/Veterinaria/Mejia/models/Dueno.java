package com.Veterinaria.Mejia.models;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dueño/Tutor de la mascota. Entidad independiente de Cliente (facturación).
 * Contiene datos de contacto completos del responsable del animal.
 */
@Entity
@Table(name = "duenos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dueno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre del dueño es obligatorio.")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(unique = true, length = 15)
    private String dni;

    @Column(length = 15)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String direccion;

    @Column(name = "tipo_documento", length = 20)
    private String tipoDocumento = "DNI"; // DNI, CE, Pasaporte

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;

    @OneToMany(mappedBy = "dueno", fetch = FetchType.LAZY)
    private List<Paciente> mascotas;

    // Relación uno a uno con el perfil de facturación del cliente.
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", referencedColumnName = "id")
    private Cliente cliente;
}
