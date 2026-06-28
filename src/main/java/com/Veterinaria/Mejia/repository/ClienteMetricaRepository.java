package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.ClienteMetrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteMetricaRepository extends JpaRepository<ClienteMetrica, Integer> {
    Optional<ClienteMetrica> findByClienteId(Integer clienteId);

    @Query("SELECT cm FROM ClienteMetrica cm WHERE cm.diasSinVisita >= :dias ORDER BY cm.diasSinVisita DESC")
    List<ClienteMetrica> findClientesInactivos(@Param("dias") Integer dias);

    @Query("SELECT cm FROM ClienteMetrica cm ORDER BY cm.gastoTotal DESC")
    List<ClienteMetrica> findTopClientesByGasto(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT cm FROM ClienteMetrica cm WHERE cm.segmento.nombre = :segmento")
    List<ClienteMetrica> findBySegmentoNombre(@Param("segmento") String segmento);
}
