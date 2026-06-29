package com.Veterinaria.Mejia.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "apertura_cierre_caja")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AperturaCierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "monto_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "monto_final", precision = 10, scale = 2)
    private BigDecimal montoFinal;

    @Column(name = "total_ingresos", precision = 10, scale = 2)
    private BigDecimal totalIngresos;

    @Column(name = "total_egresos", precision = 10, scale = 2)
    private BigDecimal totalEgresos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoCaja estado;

    @Column(length = 255)
    private String observaciones;

    public enum EstadoCaja {
        Abierta, Cerrada
    }

    @Transient
    public BigDecimal getDiferencia() {
        if (montoFinal == null || montoInicial == null || totalIngresos == null || totalEgresos == null) {
            return null;
        }
        BigDecimal saldoCalculado = montoInicial.add(totalIngresos).subtract(totalEgresos);
        return montoFinal.subtract(saldoCalculado);
    }

    @Transient
    public BigDecimal getSaldoActual() {
        BigDecimal inicial = montoInicial != null ? montoInicial : BigDecimal.ZERO;
        BigDecimal ingresos = totalIngresos != null ? totalIngresos : BigDecimal.ZERO;
        BigDecimal egresos = totalEgresos != null ? totalEgresos : BigDecimal.ZERO;
        return inicial.add(ingresos).subtract(egresos);
    }
}