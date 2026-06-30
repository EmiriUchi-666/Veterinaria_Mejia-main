package com.Veterinaria.Mejia.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    /**
     * FASE 7: hora programada para que el sistema cierre esta caja
     * automáticamente, sin necesidad de que alguien haga clic en "Cerrar Caja".
     * Si es null, la caja solo se cierra manualmente.
     */
    @Column(name = "hora_cierre_programada")
    private LocalTime horaCierreProgramada;

    @Column(name = "monto_final", precision = 10, scale = 2)
    private BigDecimal montoFinal;

    @Column(name = "total_ingresos", precision = 10, scale = 2)
    private BigDecimal totalIngresos;

    @Column(name = "total_egresos", precision = 10, scale = 2)
    private BigDecimal totalEgresos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AperturaCierreCaja.EstadoCaja estado;

    /** FASE 7: true si el cierre lo disparó el scheduler, no el cajero. */
    @Column(name = "cierre_automatico", nullable = false)
    @Builder.Default
    private boolean cierreAutomatico = false;

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

    /**
     * Saldo de efectivo disponible en este momento.
     * Es la ÚNICA fuente de verdad para la barrera anti-negativos:
     * ningún egreso en efectivo puede dejar este valor por debajo de cero.
     */
    @Transient
    public BigDecimal getSaldoActual() {
        BigDecimal inicial = montoInicial != null ? montoInicial : BigDecimal.ZERO;
        BigDecimal ingresos = totalIngresos != null ? totalIngresos : BigDecimal.ZERO;
        BigDecimal egresos = totalEgresos != null ? totalEgresos : BigDecimal.ZERO;
        return inicial.add(ingresos).subtract(egresos);
    }
}