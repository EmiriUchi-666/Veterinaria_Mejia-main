package com.Veterinaria.Mejia.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "detalles_venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @NotNull(message = "La cantidad es obligatoria.")
    @DecimalMin(value = "0.01", message = "La cantidad a vender debe ser mayor a cero.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad; // Protege la venta fraccionada (ej: mínimo 0.01 kg)

    @NotNull(message = "El precio unitario es obligatorio.")
    @DecimalMin(value = "0.00", message = "El precio unitario no puede ser negativo.")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "El subtotal es obligatorio.")
    @DecimalMin(value = "0.00", message = "El subtotal no puede ser negativo.")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}