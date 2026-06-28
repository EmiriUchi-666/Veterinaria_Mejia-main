package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Integer> {
    List<MovimientoCaja> findByCajaIdOrderByFechaHoraDesc(Integer cajaId);
}
