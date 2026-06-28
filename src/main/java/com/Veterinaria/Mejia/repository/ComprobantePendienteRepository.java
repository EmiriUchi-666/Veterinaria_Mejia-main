package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.ComprobantePendiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComprobantePendienteRepository extends JpaRepository<ComprobantePendiente, Integer> {
    List<ComprobantePendiente> findByEstadoOrderByFechaCreacionAsc(ComprobantePendiente.EstadoPendiente estado);
    long countByEstado(ComprobantePendiente.EstadoPendiente estado);
}
