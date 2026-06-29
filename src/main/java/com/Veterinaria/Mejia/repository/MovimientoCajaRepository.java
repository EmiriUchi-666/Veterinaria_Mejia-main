package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Integer> {
    List<MovimientoCaja> findByCajaIdOrderByFechaHoraDesc(Integer cajaId);
}
