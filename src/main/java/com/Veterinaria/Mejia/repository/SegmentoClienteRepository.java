package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.SegmentoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SegmentoClienteRepository extends JpaRepository<SegmentoCliente, Integer> {
    Optional<SegmentoCliente> findByNombre(String nombre);
}
