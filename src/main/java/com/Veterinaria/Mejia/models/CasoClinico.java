package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * M1/M2 — Base de datos de casos clínicos para entrenamiento del CDSS.
 * Cada consulta exitosa genera un caso que alimenta el modelo de IA.
 */
@Entity
@Table(name = "casos_clinicos")
public class CasoClinico {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    // Features para el modelo ML
    @Column(name = "especie", length = 50)
    private String especie;

    @Column(name = "raza", length = 100)
    private String raza;

    @Column(name = "edad_meses")
    private Integer edadMeses;

    @Column(name = "peso_kg", precision = 6, scale = 2)
    private java.math.BigDecimal pesoKg;

    @Column(name = "temperatura")
    private Double temperatura;

    @Column(name = "sintomas_json", columnDefinition = "TEXT")
    private String sintomasJson; // ["diarrea","vomito","fiebre"]

    // Target (diagnóstico real del veterinario)
    @Column(name = "diagnostico_veterinario", length = 200)
    private String diagnosticoVeterinario;

    @Column(name = "diagnostico_ia", length = 200)
    private String diagnosticoIA; // Lo que predijo la IA

    @Column(name = "coincidio_ia")
    private Boolean coincidioIA; // true si IA acertó

    @Column(name = "tratamiento_aplicado", columnDefinition = "TEXT")
    private String tratamientoAplicado;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado")
    private ResultadoCaso resultado;

    @Column(name = "observaciones_veterinario", columnDefinition = "TEXT")
    private String observacionesVeterinario;

    @Column(name = "fecha_caso")
    private LocalDateTime fechaCaso = LocalDateTime.now();

    @Column(name = "anonimizado")
    private Boolean anonimizado = false;

    public enum ResultadoCaso {
        EXITOSO, PARCIAL, FALLO, EN_SEGUIMIENTO
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente p) { this.paciente = p; }
    public String getEspecie() { return especie; }
    public void setEspecie(String e) { this.especie = e; }
    public String getRaza() { return raza; }
    public void setRaza(String r) { this.raza = r; }
    public Integer getEdadMeses() { return edadMeses; }
    public void setEdadMeses(Integer e) { this.edadMeses = e; }
    public java.math.BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(java.math.BigDecimal p) { this.pesoKg = p; }
    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double t) { this.temperatura = t; }
    public String getSintomasJson() { return sintomasJson; }
    public void setSintomasJson(String s) { this.sintomasJson = s; }
    public String getDiagnosticoVeterinario() { return diagnosticoVeterinario; }
    public void setDiagnosticoVeterinario(String d) { this.diagnosticoVeterinario = d; }
    public String getDiagnosticoIA() { return diagnosticoIA; }
    public void setDiagnosticoIA(String d) { this.diagnosticoIA = d; }
    public Boolean getCoincidioIA() { return coincidioIA; }
    public void setCoincidioIA(Boolean c) { this.coincidioIA = c; }
    public String getTratamientoAplicado() { return tratamientoAplicado; }
    public void setTratamientoAplicado(String t) { this.tratamientoAplicado = t; }
    public ResultadoCaso getResultado() { return resultado; }
    public void setResultado(ResultadoCaso r) { this.resultado = r; }
    public String getObservacionesVeterinario() { return observacionesVeterinario; }
    public void setObservacionesVeterinario(String o) { this.observacionesVeterinario = o; }
    public LocalDateTime getFechaCaso() { return fechaCaso; }
    public void setFechaCaso(LocalDateTime f) { this.fechaCaso = f; }
    public Boolean getAnonimizado() { return anonimizado; }
    public void setAnonimizado(Boolean a) { this.anonimizado = a; }
}
