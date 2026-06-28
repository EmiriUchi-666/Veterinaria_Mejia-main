package com.Veterinaria.Mejia.services;

import com.Veterinaria.Mejia.repository.HistoriaClinicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Servicio de estadísticas del módulo de diagnóstico clínico.
 * Provee datos para el dashboard de IA.
 */
@Service
public class DiagnosticoService {

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepo;

    /** Cuenta el total de diagnósticos (historias clínicas) registrados. */
    public long contarDiagnosticos() {
        return historiaClinicaRepo.count();
    }

    /** Etiquetas para el gráfico de dona de diagnósticos por categoría. */
    public List<String> obtenerLabels() {
        return Arrays.asList("Digestivo", "Respiratorio", "Ortopédico", "Dermatológico", "Preventivo", "Otro");
    }

    /**
     * Datos (%) para el gráfico de dona.
     * En producción se obtendría con una query agrupando por tipo de diagnóstico.
     */
    public List<Integer> obtenerDatos() {
        return Arrays.asList(25, 20, 15, 18, 12, 10);
    }
}
