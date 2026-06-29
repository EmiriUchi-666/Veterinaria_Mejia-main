package com.Veterinaria.Mejia.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.Veterinaria.Mejia.models.CasoClinico;

public interface CasoClinicoRepository extends JpaRepository<CasoClinico, Integer> {
    List<CasoClinico> findByDiagnosticoVeterinarioNotNull();
    @Query("SELECT COUNT(c) FROM CasoClinico c WHERE c.coincidioIA = true")
    long contarAciertosIA();
    long count();
}
