package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detalle de cada medicamento/producto aplicado dentro de un tratamiento.
 * Registra el lote, cantidad usada y descuenta automáticamente el stock.
 */
@Entity
@Table(name = "detalle_tratamiento")
public class DetalleTratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tratamiento_id", nullable = false)
    private Tratamiento tratamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_usada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadUsada;

    @Column(length = 50)
    private String lote;

    @Column(name = "fecha_aplicacion")
    private LocalDateTime fechaAplicacion = LocalDateTime.now();

    @Column(length = 255)
    private String observaciones;

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Tratamiento getTratamiento() { return tratamiento; }
    public void setTratamiento(Tratamiento tratamiento) { this.tratamiento = tratamiento; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public BigDecimal getCantidadUsada() { return cantidadUsada; }
    public void setCantidadUsada(BigDecimal cantidadUsada) { this.cantidadUsada = cantidadUsada; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public LocalDateTime getFechaAplicacion() { return fechaAplicacion; }
    public void setFechaAplicacion(LocalDateTime fechaAplicacion) { this.fechaAplicacion = fechaAplicacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
