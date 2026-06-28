package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_comprobante")
public class DetalleComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private ComprobanteElectronico comprobante;

    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 250)
    private String descripcion;

    @Column(length = 10)
    private String unidadMedida = "ZZ"; // ZZ=unidad servicio, KGM=kg

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "valor_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUnitario; // sin IGV

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario; // con IGV

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public ComprobanteElectronico getComprobante() { return comprobante; }
    public void setComprobante(ComprobanteElectronico c) { this.comprobante = c; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String c) { this.codigo = c; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String u) { this.unidadMedida = u; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal c) { this.cantidad = c; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal v) { this.valorUnitario = v; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p) { this.precioUnitario = p; }
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal t) { this.total = t; }
}
