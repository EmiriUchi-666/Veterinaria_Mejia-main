package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Usuario veterinario;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    @Column(name = "es_visita_externa")
    private Boolean esVisitaExterna = false;

    @Column(name = "direccion_visita")
    private String direccionVisita;

    @Column(name = "referencia_ubicacion")
    private String referenciaUbicacion;

    @Column(name = "costo_movilidad", precision = 10, scale = 2)
    private BigDecimal costoMovilidad = BigDecimal.ZERO;

    public enum EstadoCita {
        Pendiente, Confirmada, Atendida, Cancelada, No_Asistió
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Usuario getVeterinario() { return veterinario; }
    public void setVeterinario(Usuario veterinario) { this.veterinario = veterinario; }

    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }

    public Boolean getEsVisitaExterna() { return esVisitaExterna; }
    public void setEsVisitaExterna(Boolean esVisitaExterna) { this.esVisitaExterna = esVisitaExterna; }

    public String getDireccionVisita() { return direccionVisita; }
    public void setDireccionVisita(String direccionVisita) { this.direccionVisita = direccionVisita; }

    public String getReferenciaUbicacion() { return referenciaUbicacion; }
    public void setReferenciaUbicacion(String referenciaUbicacion) { this.referenciaUbicacion = referenciaUbicacion; }

    public BigDecimal getCostoMovilidad() { return costoMovilidad; }
    public void setCostoMovilidad(BigDecimal costoMovilidad) { this.costoMovilidad = costoMovilidad; }
}