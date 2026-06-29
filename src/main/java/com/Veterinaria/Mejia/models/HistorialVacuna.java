package com.Veterinaria.Mejia.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * A4 — Historial de vacunas con trazabilidad SENASA.
 * Incluye lote, laboratorio fabricante y registro sanitario.
 */
@Entity
@Data
@Table(name = "historial_vacunas")
public class HistorialVacuna {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "nombre_vacuna", nullable = false, length = 100)
    private String nombreVacuna;

    @Column(name = "enfermedad_objetivo", length = 200)
    private String enfermedadObjetivo; // "Parvovirus, Moquillo, Hepatitis"

    // ── Trazabilidad SENASA ──────────────────────────────────────────────────
    @Column(name = "lote_vacuna", length = 50)
    private String loteVacuna;

    @Column(name = "laboratorio_fabricante", length = 150)
    private String laboratorioFabricante;

    @Column(name = "registro_sanitario_senasa", length = 50)
    private String registroSanitarioSenasa;

    @Column(name = "numero_serie", length = 50)
    private String numeroSerie;

    @Column(name = "fecha_fabricacion")
    private LocalDate fechaFabricacion;

    @Column(name = "fecha_vencimiento_vacuna")
    private LocalDate fechaVencimientoVacuna;

    // ── Aplicación ───────────────────────────────────────────────────────────
    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDate fechaAplicacion;

    @Column(name = "fecha_proximo_refuerzo", nullable = false)
    private LocalDate fechaProximoRefuerzo;

    @Column(name = "dosis_ml")
    private Double dosisMl;

    @Column(name = "via_aplicacion", length = 30)
    private String viaAplicacion; // SC, IM, Intranasal

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    private Usuario veterinario;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto productoVacuna;
}
