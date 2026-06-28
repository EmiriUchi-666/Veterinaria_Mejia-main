package com.Veterinaria.Mejia.repository;
import com.Veterinaria.Mejia.models.CasoClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CasoClinicoRepository extends JpaRepository<CasoClinico, Integer> {
    List<CasoClinico> findByDiagnosticoVeterinarioNotNull();
    @Query("SELECT COUNT(c) FROM CasoClinico c WHERE c.coincidioIA = true")
    long contarAciertosIA();
    long count();
}
