package com.Veterinaria.Mejia.repository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.Veterinaria.Mejia.models.Cita;

public interface CitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByFechaHoraBetweenOrderByFechaHoraAsc(LocalDateTime inicio, LocalDateTime fin);
    List<Cita> findByPacienteIdOrderByFechaHoraDesc(Integer pacienteId);
    
    @Query("SELECT c FROM Cita c WHERE c.fechaHora BETWEEN :inicio AND :fin AND c.estado = 'Pendiente'")
    List<Cita> findCitasPendientes(LocalDateTime inicio, LocalDateTime fin);
}