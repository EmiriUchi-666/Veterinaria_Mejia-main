package com.Veterinaria.Mejia.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "productos")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especie_id")
    private Especie especie;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no debe superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "El tipo de unidad es obligatorio")
    @Column(name = "tipo_unidad", nullable = false, length = 15)
    private String tipoUnidad;

    @NotNull(message = "El precio de inversión es obligatorio")
    @Column(name = "precio_inversion", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioInversion;

    @NotNull(message = "El precio de venta es obligatorio")
    @Column(name = "precio_venta_actual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVentaActual;

    @Transient // Campo calculado, no se persiste en BD.
    private BigDecimal stockTotal;

    @Column(name = "stock_minimo", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockMinimo = new BigDecimal("12.00"); // Eliminado 'final'

    @Column(name = "estado", nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private Boolean estado = true;

    @jakarta.persistence.Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    // ── Fraccionamiento ──────────────────────────────────────────────────────
    @Column(name = "permite_fraccionamiento", columnDefinition = "TINYINT(1)")
    @Builder.Default
    private Boolean permiteFraccionamiento = false;

    /** Sacos/envases sellados enteros sin abrir */
    @Column(name = "stock_cerrado")
    @Builder.Default
    private Integer stockCerrado = 0;

    /** Kg/gramos sueltos del último envase abierto (0.00 – contenidoPorEnvase) */
    @Column(name = "stock_abierto", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockAbierto = BigDecimal.ZERO;

    /** Cuántos kg trae cada envase/saco */
    @Column(name = "contenido_por_envase", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal contenidoPorEnvase = BigDecimal.ZERO;

    /** Precio por kg/fracción que cobra el administrador */
    @Column(name = "precio_por_fraccion", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal precioPorFraccion = BigDecimal.ZERO;

    /** Indica si el producto es un alimento para mascotas (para recomendaciones IA) */
    @Column(name = "es_alimento", columnDefinition = "TINYINT(1)")
    @Builder.Default
    private Boolean esAlimento = false;

    // ── Clasificación de Uso ─────────────────────────────────────────────────
    /** Indica si el producto es exclusivo para uso clínico (no para venta en caja) */
    @Column(name = "uso_clinico", columnDefinition = "TINYINT(1)")
    @Builder.Default
    private Boolean usoClinico = false;

    /** Peso en kg de un producto suelto (para calcular kg totales) */
    @Column(name = "peso_unitario_kg", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pesoUnitarioKg = BigDecimal.ZERO;

    // ── Métodos calculados ───────────────────────────────────────────────────

    /**
     * Calcula el stock total disponible en tiempo de ejecución.
     * - Si permite fraccionamiento: (unidades cerradas * contenido por envase) + stock abierto.
     * - Si no permite fraccionamiento: stock de unidades cerradas.
     * Este método reemplaza al campo 'stockTotal' persistido.
     */
    public BigDecimal getStockTotal() {
        if (Boolean.TRUE.equals(permiteFraccionamiento)) {
            BigDecimal contenido = (contenidoPorEnvase != null && contenidoPorEnvase.compareTo(BigDecimal.ZERO) > 0) ? contenidoPorEnvase : BigDecimal.ONE;
            BigDecimal stockDeCerrados = new BigDecimal(stockCerrado != null ? stockCerrado : 0).multiply(contenido);
            BigDecimal stockDeAbierto = stockAbierto != null ? stockAbierto : BigDecimal.ZERO;
            return stockDeCerrados.add(stockDeAbierto).setScale(2, RoundingMode.HALF_UP);
        } else {
            // Para productos no fraccionables, el stock son las unidades enteras.
            return new BigDecimal(stockCerrado != null ? stockCerrado : 0).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public BigDecimal calcularInversionTotal() {
        if (stockTotal == null || precioInversion == null) return BigDecimal.ZERO;
        return stockTotal.multiply(precioInversion);
    }
}
