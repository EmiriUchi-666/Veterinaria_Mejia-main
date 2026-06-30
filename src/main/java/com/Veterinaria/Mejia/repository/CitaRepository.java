package com.Veterinaria.Mejia.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Veterinaria.Mejia.models.Cita;

public interface CitaRepository extends JpaRepository<Cita, Integer> {
    
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraAsc(LocalDateTime inicio, LocalDateTime fin);
    
    List<Cita> findByPacienteIdOrderByFechaHoraDesc(Integer pacienteId);
    
    @Query("SELECT c FROM Cita c WHERE c.fechaHora BETWEEN :inicio AND :fin AND c.estado = 'Pendiente'")
    List<Cita> findCitasPendientes(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
    
    // --- CORRECCIÓN AQUÍ ---
    // Le decimos exactamente a Spring qué consulta SQL/JPQL ejecutar.
    @Query("SELECT c FROM Cita c LEFT JOIN FETCH c.paciente LEFT JOIN FETCH c.veterinario LEFT JOIN FETCH c.servicio WHERE c.id = :id")
    Optional<Cita> findByIdWithDetalles(@Param("id") Integer id);
}