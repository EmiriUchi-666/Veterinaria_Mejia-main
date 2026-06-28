package com.Veterinaria.Mejia.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.Veterinaria.Mejia.models.IngresoStock;

@Repository
public interface IngresoStockRepository extends JpaRepository<IngresoStock, Integer> {

    // Trae los últimos 10 ingresos generales ordenados por la fecha más reciente
    List<IngresoStock> findTop10ByOrderByFechaIngresoDesc();
}