package com.Veterinaria.Mejia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Veterinaria.Mejia.models.AperturaCierreCaja;

public interface AperturaCierreCajaRepository extends JpaRepository<AperturaCierreCaja, Integer> {
    Optional<AperturaCierreCaja> findFirstByEstado(AperturaCierreCaja.EstadoCaja estado);
    List<AperturaCierreCaja> findTop10ByOrderByFechaAperturaDesc();
}
