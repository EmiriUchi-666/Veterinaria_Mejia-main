package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * A4 — Historial de vacunas con trazabilidad SENASA.
 * Incluye lote, laboratorio fabricante y registro sanitario.
 */
@Entity
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

    // ── Getters & Setters ────────────────────────────────────────────────────
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public String getNombreVacuna() { return nombreVacuna; }
    public void setNombreVacuna(String n) { this.nombreVacuna = n; }
    public String getEnfermedadObjetivo() { return enfermedadObjetivo; }
    public void setEnfermedadObjetivo(String e) { this.enfermedadObjetivo = e; }
    public String getLoteVacuna() { return loteVacuna; }
    public void setLoteVacuna(String l) { this.loteVacuna = l; }
    public String getLaboratorioFabricante() { return laboratorioFabricante; }
    public void setLaboratorioFabricante(String l) { this.laboratorioFabricante = l; }
    public String getRegistroSanitarioSenasa() { return registroSanitarioSenasa; }
    public void setRegistroSanitarioSenasa(String r) { this.registroSanitarioSenasa = r; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String n) { this.numeroSerie = n; }
    public LocalDate getFechaFabricacion() { return fechaFabricacion; }
    public void setFechaFabricacion(LocalDate f) { this.fechaFabricacion = f; }
    public LocalDate getFechaVencimientoVacuna() { return fechaVencimientoVacuna; }
    public void setFechaVencimientoVacuna(LocalDate f) { this.fechaVencimientoVacuna = f; }
    public LocalDate getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDate f) { this.fechaAplicacion = f; }
    public LocalDate getFechaProximoRefuerzo() { return fechaProximoRefuerzo; }
    public void setFechaProximoRefuerzo(LocalDate f) { this.fechaProximoRefuerzo = f; }
    public Double getDosisMl() { return dosisMl; }
    public void setDosisMl(Double d) { this.dosisMl = d; }
    public String getViaAplicacion() { return viaAplicacion; }
    public void setViaAplicacion(String v) { this.viaAplicacion = v; }
    public Usuario getVeterinario() { return veterinario; }
    public void setVeterinario(Usuario v) { this.veterinario = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String o) { this.observaciones = o; }
}
