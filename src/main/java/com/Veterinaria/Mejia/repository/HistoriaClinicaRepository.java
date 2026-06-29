package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Veterinaria.Mejia.dto.DiagnosticoCount;
import com.Veterinaria.Mejia.models.HistoriaClinica;

@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Integer> {
    List<HistoriaClinica> findByPacienteIdOrderByFechaAtencionDesc(Integer pacienteId);

    /**
     * FASE 4: Agrupa los diagnósticos y cuenta las ocurrencias de cada uno
     * para alimentar el dashboard de IA con datos reales.
     */
    @Query("SELECT new com.Veterinaria.Mejia.dto.DiagnosticoCount(h.diagnostico, COUNT(h)) FROM HistoriaClinica h WHERE h.diagnostico IS NOT NULL AND h.diagnostico <> '' GROUP BY h.diagnostico ORDER BY COUNT(h) DESC")
    List<DiagnosticoCount> countByDiagnostico();
}