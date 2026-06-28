package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Métricas calculadas por cliente para el módulo CRM.
 * Incluye frecuencia de visitas, gasto total, segmento y días sin visitar.
 */
@Entity
@Table(name = "cliente_metrica")
public class ClienteMetrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_calculo")
    private LocalDate fechaCalculo = LocalDate.now();

    @Column(name = "total_visitas")
    private Integer totalVisitas = 0;

    @Column(name = "gasto_total")
    private Double gastoTotal = 0.00;

    @Column(name = "gasto_promedio")
    private Double gastoPromedio = 0.00;

    @Column(name = "ultima_visita")
    private LocalDateTime ultimaVisita;

    @Column(name = "dias_sin_visita")
    private Integer diasSinVisita = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segmento_id")
    private SegmentoCliente segmento;

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDate getFechaCalculo() { return fechaCalculo; }
    public void setFechaCalculo(LocalDate fechaCalculo) { this.fechaCalculo = fechaCalculo; }

    public Integer getTotalVisitas() { return totalVisitas; }
    public void setTotalVisitas(Integer totalVisitas) { this.totalVisitas = totalVisitas; }

    public Double getGastoTotal() { return gastoTotal; }
    public void setGastoTotal(Double gastoTotal) { this.gastoTotal = gastoTotal; }

    public Double getGastoPromedio() { return gastoPromedio; }
    public void setGastoPromedio(Double gastoPromedio) { this.gastoPromedio = gastoPromedio; }

    public LocalDateTime getUltimaVisita() { return ultimaVisita; }
    public void setUltimaVisita(LocalDateTime ultimaVisita) { this.ultimaVisita = ultimaVisita; }

    public Integer getDiasSinVisita() { return diasSinVisita; }
    public void setDiasSinVisita(Integer diasSinVisita) { this.diasSinVisita = diasSinVisita; }

    public SegmentoCliente getSegmento() { return segmento; }
    public void setSegmento(SegmentoCliente segmento) { this.segmento = segmento; }
}
