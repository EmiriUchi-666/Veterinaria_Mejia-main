package com.Veterinaria.Mejia.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alertas_sistema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAlerta tipo;

    @Column(nullable = false, length = 255)
    private String mensaje;

    @Column(name = "entidad_id")
    private Integer entidadId;

    @Column(name = "entidad_tipo", length = 50)
    private String entidadTipo; // "Paciente", "Producto", "Cita"

    @Column(name = "fecha_generada", nullable = false)
    private LocalDateTime fechaGenerada;

    @Builder.Default
    private boolean leida = false;

    public enum TipoAlerta {
        VACUNA_VENCE,
        STOCK_BAJO,
        CITA_NO_ATENDIDA,
        BAJA_ROTACION
    }
}