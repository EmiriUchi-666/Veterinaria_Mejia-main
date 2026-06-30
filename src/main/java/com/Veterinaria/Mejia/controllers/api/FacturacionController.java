package com.Veterinaria.Mejia.controllers.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Veterinaria.Mejia.dto.FacturacionEstadoDTO;
import com.Veterinaria.Mejia.models.FacturacionEstado;
import com.Veterinaria.Mejia.repository.FacturacionEstadoRepository;
import com.Veterinaria.Mejia.services.FacturacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/facturacion")
@RequiredArgsConstructor
@Tag(name = "Facturación Electrónica", description = "Gestión de estados y reintentos de comprobantes")
public class FacturacionController {

    private final FacturacionEstadoRepository facturacionEstadoRepository;
    private final FacturacionService facturacionService;

    @GetMapping("/estados")
    @Operation(summary = "Listar historial de estados de facturación",
               description = "Devuelve una lista paginada de todos los intentos de facturación, mostrando el más reciente primero.")
    public ResponseEntity<Page<FacturacionEstado>> listarEstados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<FacturacionEstado> estados = facturacionEstadoRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaIntento"))
        );
        return ResponseEntity.ok(estados);
    }

    @PostMapping("/estados/{id}/reintentar")
    @Operation(summary = "Reintentar envío de un comprobante fallido",
               description = "Permite reenviar a Nubefact un comprobante que previamente resultó en ERROR o fue RECHAZADO.")
    public ResponseEntity<FacturacionEstadoDTO> reintentarEnvio(@PathVariable Long id) {
        FacturacionEstado estado = facturacionEstadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estado de facturación no encontrado con ID: " + id));

        if (estado.getEstado() != FacturacionEstado.EstadoFacturacion.ERROR &&
            estado.getEstado() != FacturacionEstado.EstadoFacturacion.RECHAZADO) {
            throw new IllegalArgumentException("Solo se pueden reintentar comprobantes con estado ERROR o RECHAZADO.");
        }

        // 1. Reintentar el envío, lo que devuelve la entidad actualizada.
        FacturacionEstado estadoActualizado = facturacionService.reintentarEnvioNubefact(estado);
        // 2. Convertir la entidad a un DTO antes de devolverla en la respuesta.
        return ResponseEntity.ok(FacturacionEstadoDTO.fromEntity(estadoActualizado));
    }
}