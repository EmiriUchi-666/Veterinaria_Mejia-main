package com.Veterinaria.Mejia.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.Veterinaria.Mejia.models.Venta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class NotaCredito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "venta_original_id", nullable = false)
    private Venta ventaOriginal;

    @Column(nullable = false)
    private String serie;

    @Column(nullable = false)
    private Integer correlativo;

    private LocalDateTime fechaEmision;
    private String motivo;
    private BigDecimal total;
}