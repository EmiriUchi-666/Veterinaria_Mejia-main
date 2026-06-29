package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.Veterinaria.Mejia.models.Tratamiento;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Integer> {
    List<Tratamiento> findByHistoriaClinicaPacienteIdOrderByFechaInicioDesc(Integer pacienteId);
    List<Tratamiento> findByEstado(Tratamiento.EstadoTratamiento estado);

    @Query("SELECT COUNT(t) FROM Tratamiento t WHERE t.estado = 'Activo'")
    long contarTratamientosActivos();
}
