package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.Veterinaria.Mejia.models.AlertaSistema;

public interface AlertaSistemaRepository extends JpaRepository<AlertaSistema, Long> {
    List<AlertaSistema> findByLeidaFalseOrderByFechaGeneradaDesc();
    long countByLeidaFalse();
    @Modifying
    @Query("DELETE FROM AlertaSistema a WHERE a.leida = false")
    void deleteUnreadAlerts();
}