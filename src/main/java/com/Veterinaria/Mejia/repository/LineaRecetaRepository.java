package com.Veterinaria.Mejia.repository;
import com.Veterinaria.Mejia.models.LineaReceta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface LineaRecetaRepository extends JpaRepository<LineaReceta, Integer> {
    List<LineaReceta> findByRecetaId(Integer recetaId);
}
