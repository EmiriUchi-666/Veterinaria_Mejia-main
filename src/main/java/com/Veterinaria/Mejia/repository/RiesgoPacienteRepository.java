package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.RiesgoPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RiesgoPacienteRepository extends JpaRepository<RiesgoPaciente, Integer> {
    Optional<RiesgoPaciente> findFirstByPacienteIdOrderByFechaEvaluacionDesc(Integer pacienteId);
    long countByNivelRiesgoGeneralGreaterThanEqual(Integer nivel);
}
