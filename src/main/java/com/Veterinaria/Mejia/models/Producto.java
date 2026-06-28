package com.Veterinaria.Mejia.models;

import java.math.BigDecimal;
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
    @Size(max = 100, message = "El nombre no debe superar los 100 caracteres")
    @Column(nullable = false, length = 100)
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

    @Column(name = "stock_total", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockTotal = BigDecimal.ZERO;

    @Column(name = "stock_minimo", precision = 10, scale = 2, updatable = false)
    @Builder.Default
    private BigDecimal stockMinimo = new BigDecimal("12.00");

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

    /** Peso en kg de un producto suelto (para calcular kg totales) */
    @Column(name = "peso_unitario_kg", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pesoUnitarioKg = BigDecimal.ZERO;

    // ── Métodos calculados ───────────────────────────────────────────────────

    /** Kg totales = (sacos cerrados × contenido) + stock abierto */
    public BigDecimal getKgTotales() {
        if (!Boolean.TRUE.equals(permiteFraccionamiento)) return stockTotal;
        BigDecimal kgCerrados = contenidoPorEnvase
                .multiply(new BigDecimal(stockCerrado != null ? stockCerrado : 0));
        BigDecimal kgAbiertos = stockAbierto != null ? stockAbierto : BigDecimal.ZERO;
        return kgCerrados.add(kgAbiertos);
    }

    public BigDecimal calcularInversionTotal() {
        if (stockTotal == null || precioInversion == null) return BigDecimal.ZERO;
        return stockTotal.multiply(precioInversion);
    }
}
