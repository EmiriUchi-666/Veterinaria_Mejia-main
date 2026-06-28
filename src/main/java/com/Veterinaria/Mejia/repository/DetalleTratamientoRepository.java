package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.DetalleTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleTratamientoRepository extends JpaRepository<DetalleTratamiento, Integer> {
    List<DetalleTratamiento> findByTratamientoId(Integer tratamientoId);
    List<DetalleTratamiento> findByProductoId(Integer productoId);
}
