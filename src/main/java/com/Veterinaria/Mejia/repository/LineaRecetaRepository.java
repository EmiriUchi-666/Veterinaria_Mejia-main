package com.Veterinaria.Mejia.repository;
import com.Veterinaria.Mejia.models.LineaReceta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LineaRecetaRepository extends JpaRepository<LineaReceta, Integer> {
    List<LineaReceta> findByRecetaId(Integer recetaId);
}
