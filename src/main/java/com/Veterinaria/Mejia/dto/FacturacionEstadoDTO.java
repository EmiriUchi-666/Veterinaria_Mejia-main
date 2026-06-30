package com.Veterinaria.Mejia.dto;

import java.time.LocalDateTime;

import com.Veterinaria.Mejia.models.FacturacionEstado;
import com.Veterinaria.Mejia.models.Venta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturacionEstadoDTO {
    private Long id;
    private Integer ventaId;
    private String serieCorrelativo;
    private FacturacionEstado.EstadoFacturacion estado;
    private LocalDateTime fechaIntento;
    private String respuestaApi;
    private String urlPdf;

    public static FacturacionEstadoDTO fromEntity(FacturacionEstado entity) {
        if (entity == null) {
            return null;
        }
        Venta venta = entity.getVenta();
        return FacturacionEstadoDTO.builder()
                .id(entity.getId())
                .ventaId(venta != null ? venta.getId() : null)
                .serieCorrelativo(venta != null ? venta.getSerie() + "-" + venta.getCorrelativo() : "N/A")
                .estado(entity.getEstado())
                .fechaIntento(entity.getFechaIntento())
                .respuestaApi(entity.getRespuestaApi())
                .urlPdf(entity.getUrlPdf())
                .build();
    }
}