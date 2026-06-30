package com.Veterinaria.Mejia.models;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Representa un tratamiento médico aplicado a un paciente dentro de su historia clínica.
 * Permite la trazabilidad completa de los medicamentos utilizados.
 */
@Entity
@Data
@Table(name = "tratamientos")
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historia_clinica_id", nullable = true) // Puede ser nulo para tratamientos generales
    private HistoriaClinica historiaClinica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = true)
    private Paciente pacienteTratado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTratamiento estado = EstadoTratamiento.Activo;

    @OneToMany(mappedBy = "tratamiento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleTratamiento> detalles;

    public enum EstadoTratamiento {
        Activo, Completado, Suspendido
    }
}
