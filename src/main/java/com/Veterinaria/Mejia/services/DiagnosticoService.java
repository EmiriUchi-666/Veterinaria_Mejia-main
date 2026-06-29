package com.Veterinaria.Mejia.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.Veterinaria.Mejia.dto.DiagnosticoCount;
import com.Veterinaria.Mejia.repository.HistoriaClinicaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de estadísticas del módulo de diagnóstico clínico.
 * Provee datos para el dashboard de IA.
 */
@Service
@RequiredArgsConstructor
public class DiagnosticoService {

    private final HistoriaClinicaRepository historiaClinicaRepo;

    /** Cuenta el total de diagnósticos (historias clínicas) registrados. */
    public long contarDiagnosticos() {
        return historiaClinicaRepo.count();
    }

    /** FASE 4: Etiquetas para el gráfico de dona, obtenidas de la BD. */
    public List<String> obtenerLabels() {
        return historiaClinicaRepo.countByDiagnostico().stream()
                .map(DiagnosticoCount::getDiagnostico)
                .collect(Collectors.toList());
    }

    /**
     * FASE 4: Datos para el gráfico de dona, obtenidos de la BD.
     */
    public List<Long> obtenerDatos() {
        return historiaClinicaRepo.countByDiagnostico().stream()
                .map(DiagnosticoCount::getCount)
                .collect(Collectors.toList());
    }
}
