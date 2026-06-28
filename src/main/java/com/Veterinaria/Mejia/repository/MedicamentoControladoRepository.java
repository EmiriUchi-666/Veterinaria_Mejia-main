package com.Veterinaria.Mejia.repository;
import com.Veterinaria.Mejia.models.MedicamentoControlado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface MedicamentoControladoRepository extends JpaRepository<MedicamentoControlado, Integer> {
    Optional<MedicamentoControlado> findByProductoId(Integer productoId);
    List<MedicamentoControlado> findByCategoria(MedicamentoControlado.CategoriaMedicamento cat);
    List<MedicamentoControlado> findByRequiereRecetaTrue();
}
