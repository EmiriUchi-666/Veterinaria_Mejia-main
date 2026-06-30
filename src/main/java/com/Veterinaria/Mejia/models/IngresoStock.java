package com.Veterinaria.Mejia.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ingreso_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngresoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    // Quien trajo la mercadería
    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    /**
     * FASE 7: cómo se le pagó al proveedor. "Efectivo" descuenta físicamente
     * de la caja abierta (con barrera anti-negativos). Cualquier otro valor
     * ("Credito", "Transferencia") no toca el saldo de caja.
     */
    @Column(name = "metodo_pago", nullable = false, length = 20)
    @Builder.Default
    private String metodoPago = "Credito";

    // LA SOLUCIÓN AL ERROR: La lista de detalles (el carrito)
    // cascade = CascadeType.ALL hace que si guardas la cabecera, se guarden los detalles solos
    @OneToMany(mappedBy = "ingresoStock", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleIngresoStock> detallesIngreso = new ArrayList<>();
}