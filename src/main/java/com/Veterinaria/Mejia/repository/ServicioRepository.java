package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Veterinaria.Mejia.models.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    List<Servicio> findByNombreServicioContainingIgnoreCase(String nombre);

    List<Servicio> findByEstadoTrue();

}