package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Veterinaria.Mejia.models.HistoriaClinica;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Integer> {
    List<HistoriaClinica> findByPacienteIdOrderByFechaAtencionDesc(Integer pacienteId);
}