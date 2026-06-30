package com.Veterinaria.Mejia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Veterinaria.Mejia.models.FacturacionEstado;

public interface FacturacionEstadoRepository extends JpaRepository<FacturacionEstado, Long> {
    // Métodos de consulta personalizados si son necesarios en el futuro
}
