package com.Veterinaria.Mejia.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.ComprobanteElectronico;
import com.Veterinaria.Mejia.models.ComprobantePendiente;
import com.Veterinaria.Mejia.repository.ComprobanteElectronicoRepository;
import com.Veterinaria.Mejia.repository.ComprobantePendienteRepository;

import lombok.RequiredArgsConstructor;

/**
 * C5 — Modo Contingencia SUNAT.
 * Guarda comprobantes fallidos en cola local y reintenta cada 5 min con
 * exponential backoff (máx. 10 intentos = ~72h operación sin conexión).
 */
@Service
@RequiredArgsConstructor
public class ContingenciaService {

    private static final Logger log = LoggerFactory.getLogger(ContingenciaService.class);
    private static final int MAX_INTENTOS = 10;

    private final ComprobantePendienteRepository pendienteRepo;
    private final ComprobanteElectronicoRepository comprobanteRepo;

    @Value("${nubefact.api.url:https://api.nubefact.com/api/v1}")
    private String apiUrl;

    @Value("${nubefact.api.token:DEMO}")
    private String apiToken;

    @Value("${nubefact.ruc:20600000001}")
    private String ruc;

    /**
     * Encolar un comprobante fallido para reintento posterior.
     */
    @Transactional
    public void encolar(ComprobanteElectronico comp, String jsonRequest, String errorInicial) {
        ComprobantePendiente pendiente = new ComprobantePendiente();
        pendiente.setComprobante(comp);
        pendiente.setJsonRequest(jsonRequest);
        pendiente.setIntentos(1);
        pendiente.setUltimoIntento(LocalDateTime.now());
        pendiente.setErrorDetalle(errorInicial);
        pendiente.setEstado(ComprobantePendiente.EstadoPendiente.PENDIENTE);
        pendienteRepo.save(pendiente);
        log.warn("[CONTINGENCIA] Comprobante {} encolado. Error: {}", comp.getNumeroCompleto(), errorInicial);
    }

    /**
     * Job programado — ejecuta cada 5 minutos.
     * Reintenta envío con exponential backoff.
     */
    @Scheduled(fixedDelay = 300_000) // 5 minutos
    @Transactional
    public void procesarCola() {
        if ("DEMO".equals(apiToken)) return; // No ejecutar en modo demo

        List<ComprobantePendiente> pendientes = pendienteRepo.findByEstadoOrderByFechaCreacionAsc(
                ComprobantePendiente.EstadoPendiente.PENDIENTE);

        if (pendientes.isEmpty()) return;
        log.info("[CONTINGENCIA] Procesando {} comprobantes en cola...", pendientes.size());

        for (ComprobantePendiente p : pendientes) {
            // Exponential backoff: esperar 2^intentos minutos entre reintentos
            long minutosEspera = (long) Math.pow(2, Math.min(p.getIntentos(), 8));
            if (p.getUltimoIntento() != null &&
                    p.getUltimoIntento().plusMinutes(minutosEspera).isAfter(LocalDateTime.now())) {
                continue; // Aún no es tiempo de reintentar
            }

            if (p.getIntentos() >= MAX_INTENTOS) {
                p.setEstado(ComprobantePendiente.EstadoPendiente.ERROR_PERMANENTE);
                p.setErrorDetalle("Superado máximo de " + MAX_INTENTOS + " intentos.");
                pendienteRepo.save(p);
                log.error("[CONTINGENCIA] Comprobante {} marcado como ERROR_PERMANENTE.", 
                    p.getComprobante().getNumeroCompleto());
                continue;
            }

            p.setEstado(ComprobantePendiente.EstadoPendiente.ENVIANDO);
            p.setIntentos(p.getIntentos() + 1);
            p.setUltimoIntento(LocalDateTime.now());
            pendienteRepo.save(p);

            try {
                String url = apiUrl + "/" + ruc + "/comprobante";
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Token " + apiToken)
                        .POST(HttpRequest.BodyPublishers.ofString(p.getJsonRequest()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    p.setEstado(ComprobantePendiente.EstadoPendiente.ENVIADO);
                    ComprobanteElectronico comp = p.getComprobante();
                    comp.setEstado(ComprobanteElectronico.EstadoComprobante.ACEPTADO);
                    comprobanteRepo.save(comp);
                    log.info("[CONTINGENCIA] Comprobante {} enviado exitosamente en intento {}.",
                            comp.getNumeroCompleto(), p.getIntentos());
                } else {
                    p.setEstado(ComprobantePendiente.EstadoPendiente.PENDIENTE);
                    p.setErrorDetalle("HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                p.setEstado(ComprobantePendiente.EstadoPendiente.PENDIENTE);
                p.setErrorDetalle("Error: " + e.getMessage());
                log.warn("[CONTINGENCIA] Intento {} fallido para {}: {}", 
                    p.getIntentos(), p.getComprobante().getNumeroCompleto(), e.getMessage());
            }
            pendienteRepo.save(p);
        }
    }

    public long contarPendientes() {
        return pendienteRepo.countByEstado(ComprobantePendiente.EstadoPendiente.PENDIENTE);
    }

    public List<ComprobantePendiente> listarCola() {
        return pendienteRepo.findByEstadoOrderByFechaCreacionAsc(ComprobantePendiente.EstadoPendiente.PENDIENTE);
    }
}
