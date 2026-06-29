package com.Veterinaria.Mejia.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.Veterinaria.Mejia.models.IngresoStock;

public interface IngresoStockRepository extends JpaRepository<IngresoStock, Integer> {

    // Trae los últimos 10 ingresos generales ordenados por la fecha más reciente
    List<IngresoStock> findTop10ByOrderByFechaIngresoDesc();
}