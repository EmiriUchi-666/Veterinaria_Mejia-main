package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.HistorialVacuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HistorialVacunaRepository extends JpaRepository<HistorialVacuna, Integer> {
    List<HistorialVacuna> findByPacienteId(Integer pacienteId);

    @Query("SELECT v FROM HistorialVacuna v WHERE v.fechaProximoRefuerzo BETWEEN :hoy AND :limite ORDER BY v.fechaProximoRefuerzo ASC")
    List<HistorialVacuna> findVacunasPendientes(@Param("hoy") LocalDate hoy, @Param("limite") LocalDate limite);

    @Query("SELECT COUNT(v) FROM HistorialVacuna v WHERE v.fechaProximoRefuerzo BETWEEN :hoy AND :limite")
    long contarVacunasPendientes(@Param("hoy") LocalDate hoy, @Param("limite") LocalDate limite);
}
