package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.ComprobanteElectronico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobanteElectronicoRepository extends JpaRepository<ComprobanteElectronico, Integer> {

    /** Último número correlativo emitido por serie (para autoincremento) */
    @Query("SELECT MAX(c.numero) FROM ComprobanteElectronico c WHERE c.serie = :serie")
    Optional<Integer> findMaxNumeroBySerie(@Param("serie") String serie);

    List<ComprobanteElectronico> findTop50ByOrderByFechaRegistroDesc();

    List<ComprobanteElectronico> findByVentaId(Integer ventaId);

    @Query("SELECT c FROM ComprobanteElectronico c WHERE c.receptorNumDoc = :doc ORDER BY c.fechaRegistro DESC")
    List<ComprobanteElectronico> findByReceptorNumDoc(@Param("doc") String doc);
}
