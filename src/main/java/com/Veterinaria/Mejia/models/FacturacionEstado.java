package com.Veterinaria.Mejia.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "facturacion_estado")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturacionEstado {

    public enum EstadoFacturacion {
        PENDIENTE,
        ENVIADO,
        ACEPTADO,
        RECHAZADO,
        ERROR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFacturacion estado;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaIntento;

    @Column(columnDefinition = "TEXT")
    private String respuestaApi;

    @Column(length = 255)
    private String urlPdf;

    @Column(length = 255)
    private String urlXml;

    @Column(length = 100)
    private String codigoHash;

    @Column(length = 10)
    private String codigoRespuestaSunat;
}
