package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.SegmentoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SegmentoClienteRepository extends JpaRepository<SegmentoCliente, Integer> {
    Optional<SegmentoCliente> findByNombre(String nombre);
}
