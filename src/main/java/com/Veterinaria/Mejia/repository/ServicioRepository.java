package com.Veterinaria.Mejia.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Veterinaria.Mejia.models.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    // =========================================================================
    // 1. NUEVA QUERY JPQL: Buscador dinámico por nombre (Gestión de Servicios)
    // =========================================================================
    @Query("SELECT s FROM Servicio s WHERE " +
           "(:nombre IS NULL OR :nombre = '' OR LOWER(s.nombreServicio) LIKE LOWER(CONCAT('%', :nombre, '%')))")
    List<Servicio> buscarYFiltrarServiciosJPQL(@Param("nombre") String nombre);

    // =========================================================================
    // 2. NUEVA QUERY JPQL: Exclusivo para el Punto de Venta (Solo Activos)
    // =========================================================================
    @Query("SELECT s FROM Servicio s WHERE s.estado = true")
    List<Servicio> listarServiciosActivosJPQL();

    // =========================================================================
    // 3. TU QUERY ORIGINAL: Filtra servicios por precio máximo
    // =========================================================================
    @Query("SELECT s FROM Servicio s WHERE s.precioServicio <= :precioMax")
    List<Servicio> buscarServiciosEconomicosJPQL(@Param("precioMax") BigDecimal precioMax);
}