package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lineas_receta")
public class LineaReceta {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private RecetaVeterinaria receta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(length = 200)
    private String medicamento; // Si no está en catálogo

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "unidad_dosis", length = 30)
    private String unidadDosis; // mg, ml, comprimido, etc.

    @Column(name = "frecuencia", length = 100)
    private String frecuencia; // "Cada 12 horas"

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    @Column(name = "via_administracion", length = 50)
    private String viaAdministracion; // Oral, IM, IV, SC

    @Column(columnDefinition = "TEXT")
    private String indicaciones;

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public RecetaVeterinaria getReceta() { return receta; }
    public void setReceta(RecetaVeterinaria r) { this.receta = r; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto p) { this.producto = p; }
    public String getMedicamento() { return medicamento; }
    public void setMedicamento(String m) { this.medicamento = m; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal c) { this.cantidad = c; }
    public String getUnidadDosis() { return unidadDosis; }
    public void setUnidadDosis(String u) { this.unidadDosis = u; }
    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String f) { this.frecuencia = f; }
    public Integer getDuracionDias() { return duracionDias; }
    public void setDuracionDias(Integer d) { this.duracionDias = d; }
    public String getViaAdministracion() { return viaAdministracion; }
    public void setViaAdministracion(String v) { this.viaAdministracion = v; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String i) { this.indicaciones = i; }
}
