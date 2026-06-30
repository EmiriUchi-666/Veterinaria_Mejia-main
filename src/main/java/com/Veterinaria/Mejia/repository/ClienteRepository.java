package com.Veterinaria.Mejia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Veterinaria.Mejia.models.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // JPQL: Busca cliente por coincidencia exacta de Documento
    // CORRECCIÓN: Se usa c.numeroDocumento en lugar de c.dni
    @Query("SELECT c FROM Cliente c WHERE c.numeroDocumento = :dni")
    Optional<Cliente> findByDniJPQL(@Param("dni") String dni);

    // JPQL: Buscador predictivo por nombre ignorando mayúsculas/minúsculas
    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Cliente> findByNombreContainingJPQL(@Param("nombre") String nombre);

    // JPQL: Validación de existencia para el formulario de registro de dueños
    // CORRECCIÓN: Se usa c.numeroDocumento en lugar de c.dni
    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.numeroDocumento = :dni")
    boolean existsByDniJPQL(@Param("dni") String dni);

    // Métodos nativos de Spring Data (estos ya funcionaban bien porque usan el nombre correcto)
    Optional<Cliente> findByNumeroDocumento(String numDocIngresado);

    boolean existsByNumeroDocumento(String numeroDocumento);
}