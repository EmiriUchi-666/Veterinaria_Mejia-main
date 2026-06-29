package com.Veterinaria.Mejia.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Veterinaria.Mejia.models.Merma;

public interface MermaRepository extends JpaRepository<Merma, Integer> {

    // Suma la pérdida económica total desde una fecha específica
    @Query("SELECT COALESCE(SUM(m.perdidaEconomica), 0) FROM Merma m WHERE m.fechaRegistro >= :fecha")
    BigDecimal calcularTotalPerdidasDesdeJPQL(@Param("fecha") LocalDateTime fecha);
}