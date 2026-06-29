package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.DetalleTratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleTratamientoRepository extends JpaRepository<DetalleTratamiento, Integer> {
    List<DetalleTratamiento> findByTratamientoId(Integer tratamientoId);
    List<DetalleTratamiento> findByProductoId(Integer productoId);
}
