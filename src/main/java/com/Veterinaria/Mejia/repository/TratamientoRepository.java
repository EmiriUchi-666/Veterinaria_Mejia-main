package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TratamientoRepository extends JpaRepository<Tratamiento, Integer> {
    List<Tratamiento> findByHistoriaClinicaPacienteIdOrderByFechaInicioDesc(Integer pacienteId);
    List<Tratamiento> findByEstado(Tratamiento.EstadoTratamiento estado);

    @Query("SELECT COUNT(t) FROM Tratamiento t WHERE t.estado = 'Activo'")
    long contarTratamientosActivos();
}
