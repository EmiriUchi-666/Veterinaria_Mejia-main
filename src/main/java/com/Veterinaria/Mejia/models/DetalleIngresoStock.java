package com.Veterinaria.Mejia.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalle_ingreso_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleIngresoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Conectamos de vuelta con la cabecera
    @ManyToOne
    @JoinColumn(name = "ingreso_stock_id", nullable = false)
    private IngresoStock ingresoStock;

    // Qué producto está entrando
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Cuánto está entrando
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;
    
    // (Opcional) Si quieres registrar a qué precio te lo vendió el proveedor en ese momento
    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;
}