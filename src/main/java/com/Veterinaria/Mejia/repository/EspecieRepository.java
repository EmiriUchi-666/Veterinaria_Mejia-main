package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.Especie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EspecieRepository extends JpaRepository<Especie, Integer> {

    // JPQL: Trae todas las especies ordenadas alfabéticamente (Útil para alimentar tus select de filtros)
    @Query("SELECT e FROM Especie e ORDER BY e.nombreEspecie ASC")
    List<Especie> listarTodasOrdenadasJPQL();
}