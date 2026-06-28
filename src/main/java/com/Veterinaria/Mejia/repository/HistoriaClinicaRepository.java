package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Integer> {
    List<HistoriaClinica> findByPacienteIdOrderByFechaAtencionDesc(Integer pacienteId);
}