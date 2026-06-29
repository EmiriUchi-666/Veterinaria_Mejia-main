package com.Veterinaria.Mejia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Veterinaria.Mejia.models.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    // JPQL: Busca un proveedor por su atributo 'ruc'
    @Query("SELECT p FROM Proveedor p WHERE p.ruc = :ruc")
    Optional<Proveedor> buscarPorRucJPQL(@Param("ruc") String ruc);
}