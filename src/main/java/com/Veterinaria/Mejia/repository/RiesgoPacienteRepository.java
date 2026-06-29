package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.RiesgoPaciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiesgoPacienteRepository extends JpaRepository<RiesgoPaciente, Integer> {
    Optional<RiesgoPaciente> findFirstByPacienteIdOrderByFechaEvaluacionDesc(Integer pacienteId);
    long countByNivelRiesgoGeneralGreaterThanEqual(Integer nivel);
}
