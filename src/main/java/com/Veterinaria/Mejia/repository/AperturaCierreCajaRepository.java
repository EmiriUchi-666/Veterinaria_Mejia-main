package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.AperturaCierreCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AperturaCierreCajaRepository extends JpaRepository<AperturaCierreCaja, Integer> {
    Optional<AperturaCierreCaja> findFirstByEstado(AperturaCierreCaja.EstadoCaja estado);
    List<AperturaCierreCaja> findTop10ByOrderByFechaAperturaDesc();
}
