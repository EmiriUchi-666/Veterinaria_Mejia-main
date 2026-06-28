package com.Veterinaria.Mejia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Veterinaria.Mejia.models.Dueno;

public interface DuenoRepository extends JpaRepository<Dueno, Integer> {

    @Query("SELECT d FROM Dueno d WHERE LOWER(d.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.dni) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.telefono) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Dueno> buscar(@Param("q") String q);

    List<Dueno> findByEstadoTrueOrderByNombreAsc();

    Optional<Dueno> findByDni(String dni);

    long countByEstadoTrue();
}