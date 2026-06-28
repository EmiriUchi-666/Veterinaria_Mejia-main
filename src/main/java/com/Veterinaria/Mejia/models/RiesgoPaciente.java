package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registra la evaluación de riesgo predictivo de un paciente.
 * Calculado por el motor de IA heurístico basado en historial clínico.
 */
@Entity
@Table(name = "riesgo_paciente")
public class RiesgoPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "fecha_evaluacion")
    private LocalDateTime fechaEvaluacion = LocalDateTime.now();

    @Column(name = "riesgo_enfermedades_recurrentes")
    private Double riesgoEnfermedadesRecurrentes = 0.0;

    @Column(name = "riesgo_recaida")
    private Double riesgoRecaida = 0.0;

    @Column(name = "riesgo_articular")
    private Double riesgoArticular = 0.0;

    @Column(name = "riesgo_cardiaco")
    private Double riesgoCardiaco = 0.0;

    @Column(name = "riesgo_alergico")
    private Double riesgoAlergico = 0.0;

    @Column(name = "enfermedades_futuras_probables", columnDefinition = "TEXT")
    private String enfermedadesFuturasProbables;

    @Column(name = "recomendaciones", columnDefinition = "TEXT")
    private String recomendaciones;

    @Column(name = "nivel_riesgo_general")
    private Integer nivelRiesgoGeneral = 1; // 1-10

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public LocalDateTime getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(LocalDateTime fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }

    public Double getRiesgoEnfermedadesRecurrentes() { return riesgoEnfermedadesRecurrentes; }
    public void setRiesgoEnfermedadesRecurrentes(Double v) { this.riesgoEnfermedadesRecurrentes = v; }

    public Double getRiesgoRecaida() { return riesgoRecaida; }
    public void setRiesgoRecaida(Double riesgoRecaida) { this.riesgoRecaida = riesgoRecaida; }

    public Double getRiesgoArticular() { return riesgoArticular; }
    public void setRiesgoArticular(Double riesgoArticular) { this.riesgoArticular = riesgoArticular; }

    public Double getRiesgoCardiaco() { return riesgoCardiaco; }
    public void setRiesgoCardiaco(Double riesgoCardiaco) { this.riesgoCardiaco = riesgoCardiaco; }

    public Double getRiesgoAlergico() { return riesgoAlergico; }
    public void setRiesgoAlergico(Double riesgoAlergico) { this.riesgoAlergico = riesgoAlergico; }

    public String getEnfermedadesFuturasProbables() { return enfermedadesFuturasProbables; }
    public void setEnfermedadesFuturasProbables(String v) { this.enfermedadesFuturasProbables = v; }

    public String getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }

    public Integer getNivelRiesgoGeneral() { return nivelRiesgoGeneral; }
    public void setNivelRiesgoGeneral(Integer nivelRiesgoGeneral) { this.nivelRiesgoGeneral = nivelRiesgoGeneral; }
}
