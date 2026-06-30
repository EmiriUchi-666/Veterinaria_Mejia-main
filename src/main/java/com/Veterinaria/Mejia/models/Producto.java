package com.Veterinaria.Mejia.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // --- Stock Management ---
    /** Stock de unidades cerradas (ej: cajas, sacos) */
    @Column(name = "stock_cerrado")
    @Builder.Default
    private Integer stockCerrado = 0;

    /** Unidades por cada unidad cerrada (ej: 12 pastillas por caja) */
    @Column(name = "unidades_por_cerrado")
    @Builder.Default
    private Integer unidadesPorCerrado = 1;

    /** Stock de unidades sueltas/fraccionadas (ej: pastillas, kg) */
    @Column(name = "stock_fraccionado", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockFraccionado = BigDecimal.ZERO;

    /** Umbral mínimo de unidades CERRADAS para generar alerta de stock bajo */
    @Column(name = "stock_minimo")
    @Builder.Default
    private Integer stockMinimo = 0;

    // --- Pricing ---
    @NotNull
    @DecimalMin("0.00")
    @Column(name = "precio_inversion", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioInversion;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "precio_venta_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVentaActual;

    /**
     * FASE 10: Precio de venta por unidad suelta (blister, pastilla, kg).
     * Puede ser autocalculado o manual.
     */
    @DecimalMin("0.00")
    @Column(name = "precio_por_fraccion", precision = 10, scale = 2)
    private BigDecimal precioPorFraccion;

    /**
     * FASE 10: Define si el producto se puede vender en unidades más pequeñas que el envase.
     */
    @Column(name = "permite_fraccionamiento")
    private Boolean permiteFraccionamiento = false;

    /**
     * FASE 10: Contenido del envase cerrado (ej: 10 para 10 blisters, 15 para 15kg).
     */
    @Column(name = "contenido_por_envase", precision = 10, scale = 2)
    private BigDecimal contenidoPorEnvase;

    /**
     * FASE 10: Unidad de medida para facturación (kg, litros, unidad, etc.).
     */
    @Column(name = "tipo_unidad", length = 20)
    private String tipoUnidad;
    // --- Categorization & Status ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especie_id")
    private Especie especie;

    @Column(nullable = false)
    @Builder.Default
    private boolean estado = true;

    /** FASE 11: Indica si el producto es un alimento. */
    @Column(name = "es_alimento")
    private Boolean esAlimento = false;

    /** FASE 10: Stock de unidades sueltas (blisters, pastillas, kg). */
    @Column(name = "stock_abierto", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockAbierto = BigDecimal.ZERO;

    @Column(name = "fecha_creacion", updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // =========================================================================
    // BUSINESS LOGIC FOR STOCK MANAGEMENT
    // =========================================================================

    /**
     * Calcula el stock total disponible en unidades fraccionadas.
     * @return El stock total como BigDecimal.
     */
    @Transient
    public BigDecimal getStockTotal() {
        // FIX: Add null checks to prevent NullPointerException on calculation
        if (Boolean.TRUE.equals(this.permiteFraccionamiento)) {
            // For fractionable products, total is stockCerrado * contenidoPorEnvase + stockAbierto
            BigDecimal cerradas = (this.stockCerrado != null) ? new BigDecimal(this.stockCerrado) : BigDecimal.ZERO;
            BigDecimal contenido = (this.contenidoPorEnvase != null) ? this.contenidoPorEnvase : BigDecimal.ZERO;
            BigDecimal abierto = (this.stockAbierto != null) ? this.stockAbierto : BigDecimal.ZERO;
            return cerradas.multiply(contenido).add(abierto);
        } else {
            // For non-fractionable, total is just stockCerrado (assuming 1 unit per cerrado)
            return (this.stockCerrado != null) ? new BigDecimal(this.stockCerrado) : BigDecimal.ZERO;
        }
    }

    /**
     * Verifica si hay suficiente stock para vender una cantidad determinada.
     * @param cantidadAVender La cantidad que se desea vender.
     * @return true si hay stock suficiente, false en caso contrario.
     */
    public boolean tieneStockSuficiente(BigDecimal cantidadAVender) {
        return getStockTotal().compareTo(cantidadAVender) >= 0;
    }

    /**
     * Disminuye el stock del producto. Lanza una excepción si no hay stock suficiente.
     * La lógica prioriza el uso de stock fraccionado antes de "abrir" una unidad cerrada.
     * @param cantidad La cantidad a descontar.
     */
    public void descontarStock(BigDecimal cantidad) {
        if (!tieneStockSuficiente(cantidad)) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + this.nombre);
        }

        if (Boolean.TRUE.equals(this.permiteFraccionamiento)) {
            BigDecimal cantidadRestante = cantidad;
            // Descontar primero del stock abierto
            if (this.stockAbierto.compareTo(cantidadRestante) >= 0) {
                this.stockAbierto = this.stockAbierto.subtract(cantidadRestante);
                return;
            }

            // Si el stock abierto no es suficiente, usarlo todo y abrir unidades cerradas
            cantidadRestante = cantidadRestante.subtract(this.stockAbierto);
            this.stockAbierto = BigDecimal.ZERO;

            // Abrir paquetes cerrados hasta que la cantidad restante sea cubierta
            while (cantidadRestante.compareTo(BigDecimal.ZERO) > 0) {
                if (this.stockCerrado == null || this.stockCerrado <= 0) {
                    throw new IllegalStateException("Stock insuficiente de unidades cerradas para fraccionar: " + this.nombre);
                }
                if (this.contenidoPorEnvase == null || this.contenidoPorEnvase.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalStateException("Configuración de contenido por envase inválida para '" + this.nombre + "'.");
                }
                this.stockCerrado -= 1;
                this.stockAbierto = this.stockAbierto.add(this.contenidoPorEnvase);
                
                // Now try to deduct from the newly opened stock
                if (this.stockAbierto.compareTo(cantidadRestante) >= 0) {
                    this.stockAbierto = this.stockAbierto.subtract(cantidadRestante);
                    cantidadRestante = BigDecimal.ZERO; // All deducted
                } else {
                    // Still not enough, deduct what's available and continue loop
                    cantidadRestante = cantidadRestante.subtract(this.stockAbierto);
                    this.stockAbierto = BigDecimal.ZERO;
                }
            }
        } else {
            // For non-fractionable products, deduct from stockCerrado
            int cantidadEntera = cantidad.intValue(); // Assuming non-fractionable means integer quantities
            if (this.stockCerrado == null || this.stockCerrado < cantidadEntera) {
                throw new IllegalStateException("Stock insuficiente de unidades enteras para: " + this.nombre);
            }
            this.stockCerrado -= cantidadEntera;
        }
    }

    /**
     * Aumenta el stock del producto, por ejemplo, al anular una venta.
     * @param cantidad La cantidad a devolver al stock.
     */
    public void devolverStock(BigDecimal cantidad) {
        // La forma más simple es añadirlo al stock fraccionado.
        // Una lógica más compleja podría intentar "re-empaquetar" en unidades cerradas.
        this.stockFraccionado = this.stockFraccionado.add(cantidad);
    }
}