package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Veterinaria.Mejia.models.ComprobantePendiente;

public interface ComprobantePendienteRepository extends JpaRepository<ComprobantePendiente, Integer> {
    List<ComprobantePendiente> findByEstadoOrderByFechaCreacionAsc(ComprobantePendiente.EstadoPendiente estado);
    long countByEstado(ComprobantePendiente.EstadoPendiente estado);
}
