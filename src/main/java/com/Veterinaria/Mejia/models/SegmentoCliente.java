package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;

/**
 * Define los segmentos de clientes para el CRM.
 * Categoriza a los clientes según frecuencia de visitas y gasto promedio.
 */
@Entity
@Table(name = "segmento_cliente")
public class SegmentoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre; // VIP, Frecuente, Ocasional, Inactivo

    @Column(name = "frecuencia_minima_visitas_anio")
    private Integer frecuenciaMinimaVisitasAnio;

    @Column(name = "gasto_promedio_minimo")
    private Double gastoPromedioMinimo;

    @Column(length = 7)
    private String color; // Color para UI (#1abc9c, #f39c12, etc.)

    @Column(length = 50)
    private String icono; // Icono Bootstrap Icons

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getFrecuenciaMinimaVisitasAnio() { return frecuenciaMinimaVisitasAnio; }
    public void setFrecuenciaMinimaVisitasAnio(Integer v) { this.frecuenciaMinimaVisitasAnio = v; }

    public Double getGastoPromedioMinimo() { return gastoPromedioMinimo; }
    public void setGastoPromedioMinimo(Double gastoPromedioMinimo) { this.gastoPromedioMinimo = gastoPromedioMinimo; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
}
