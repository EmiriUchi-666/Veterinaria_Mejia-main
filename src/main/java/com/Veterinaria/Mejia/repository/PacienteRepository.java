package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Veterinaria.Mejia.models.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    List<Paciente> findByClienteId(Integer clienteId);
    List<Paciente> findByEstadoTrue();
    Paciente findByMicrochip(String microchip);
}