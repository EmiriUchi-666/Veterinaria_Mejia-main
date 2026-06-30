package com.Veterinaria.Mejia.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.Veterinaria.Mejia.models.FacturacionEstado;

public interface FacturacionEstadoRepository extends JpaRepository<FacturacionEstado, Long> {

    // Debes agregar la anotación @Query para que JPA no intente "adivinar"
    @Query("SELECT f FROM FacturacionEstado f") // Ajusta esta consulta a lo que necesites
    Page<FacturacionEstado> findAllWithDetails(Pageable pageable);
}