package com.Veterinaria.Mejia.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "El usuario (cajero) es obligatorio para registrar la venta.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Se mantiene nullable para permitir las compras rápidas sin registrar cliente (Público General)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "fecha_emision", insertable = false, updatable = false)
    private LocalDateTime fechaEmision;

    @NotNull(message = "El total de la venta no puede ser nulo.")
    @DecimalMin(value = "0.00", message = "El total de la venta no puede ser negativo.")
    @Column(name = "total_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalVenta;

    @NotBlank(message = "Debe seleccionar un método de pago.")
    @Pattern(regexp = "^(Efectivo|Yape|Plin|Tarjeta)$", message = "Método de pago no válido.")
    @Column(name = "tipo_pago", nullable = false)
    private String tipoPago;

    @NotBlank(message = "El tipo de comprobante es obligatorio.")
    @Pattern(regexp = "^(Boleta|Ticket)$", message = "El comprobante debe ser Boleta o Ticket.")
    @Column(name = "tipo_comprobante", nullable = false)
    private String tipoComprobante;

    @NotBlank(message = "La serie del comprobante es obligatoria.")
    @Pattern(regexp = "^(B001|T001)$", message = "La serie debe ser B001 para Boletas o T001 para Tickets.")
    @Column(nullable = false, length = 4)
    private String serie;

    @NotNull(message = "El correlativo de la venta es obligatorio.")
    @Min(value = 1, message = "El correlativo debe ser un número positivo mayor a 0.")
    @Column(nullable = false)
    private Integer correlativo;

    // true = Venta Activa, false = Venta Anulada
    @Column(name = "estado", nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private Boolean estado = true;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleVenta> detallesVentas;
}