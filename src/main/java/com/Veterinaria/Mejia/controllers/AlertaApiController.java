package com.Veterinaria.Mejia.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Veterinaria.Mejia.models.AlertaSistema;
import com.Veterinaria.Mejia.repository.AlertaSistemaRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaApiController {

    private final AlertaSistemaRepository alertaSistemaRepo;

    /**
     * Devuelve todas las alertas del sistema que no han sido marcadas como leídas.
     * @return Una lista de alertas no leídas.
     */
    @GetMapping("/no-leidas")
    public List<AlertaSistema> getAlertasNoLeidas() {
        return alertaSistemaRepo.findByLeidaFalseOrderByFechaGeneradaDesc();
    }

    /**
     * Marca una alerta específica como leída.
     * @param id El ID de la alerta a marcar.
     * @return Una respuesta HTTP 200 OK si fue exitoso.
     */
    @PostMapping("/marcar-leida/{id}")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        alertaSistemaRepo.findById(id).ifPresent(alerta -> {
            alerta.setLeida(true);
            alertaSistemaRepo.save(alerta);
        });
        return ResponseEntity.ok().build();
    }
}