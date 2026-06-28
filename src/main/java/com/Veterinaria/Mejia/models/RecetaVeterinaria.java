package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A5 — Receta médica veterinaria digital.
 * Cumple con Ley 28733 (Ejercicio de la Medicina Veterinaria).
 * Incluye datos del veterinario (CMP), paciente, diagnóstico y medicamentos.
 */
@Entity
@Table(name = "recetas_veterinarias")
public class RecetaVeterinaria {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_receta", unique = true, length = 20)
    private String numeroReceta; // RV-20250001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Usuario veterinario;

    @Column(name = "cmp_veterinario", length = 20)
    private String cmpVeterinario; // N° Colegiatura CMP

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento; // Generalmente 30 días

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "indicaciones", columnDefinition = "TEXT")
    private String indicaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReceta estado = EstadoReceta.EMITIDA;

    @Column(name = "url_pdf", length = 500)
    private String urlPdf;

    @Column(name = "codigo_qr", length = 200)
    private String codigoQr; // Para verificación digital

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LineaReceta> lineas;

    public enum EstadoReceta { EMITIDA, DESPACHADA, VENCIDA, ANULADA }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNumeroReceta() { return numeroReceta; }
    public void setNumeroReceta(String n) { this.numeroReceta = n; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente p) { this.paciente = p; }
    public Usuario getVeterinario() { return veterinario; }
    public void setVeterinario(Usuario v) { this.veterinario = v; }
    public String getCmpVeterinario() { return cmpVeterinario; }
    public void setCmpVeterinario(String c) { this.cmpVeterinario = c; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate f) { this.fechaEmision = f; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate f) { this.fechaVencimiento = f; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String d) { this.diagnostico = d; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String i) { this.indicaciones = i; }
    public EstadoReceta getEstado() { return estado; }
    public void setEstado(EstadoReceta e) { this.estado = e; }
    public String getUrlPdf() { return urlPdf; }
    public void setUrlPdf(String u) { this.urlPdf = u; }
    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String c) { this.codigoQr = c; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime f) { this.fechaRegistro = f; }
    public List<LineaReceta> getLineas() { return lineas; }
    public void setLineas(List<LineaReceta> l) { this.lineas = l; }
}
