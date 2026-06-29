package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Veterinaria.Mejia.models.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    List<Paciente> findByClienteId(Integer clienteId);
    List<Paciente> findByEstadoTrue();
    Paciente findByMicrochip(String microchip);
}