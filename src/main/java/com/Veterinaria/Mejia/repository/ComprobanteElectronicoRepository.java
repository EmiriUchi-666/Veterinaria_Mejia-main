package com.Veterinaria.Mejia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Veterinaria.Mejia.models.ComprobanteElectronico;

public interface ComprobanteElectronicoRepository extends JpaRepository<ComprobanteElectronico, Integer> {

    /** Último número correlativo emitido por serie (para autoincremento) */
    @Query("SELECT MAX(c.numero) FROM ComprobanteElectronico c WHERE c.serie = :serie")
    Optional<Integer> findMaxNumeroBySerie(@Param("serie") String serie);

    List<ComprobanteElectronico> findTop50ByOrderByFechaRegistroDesc();

    List<ComprobanteElectronico> findByVentaId(Integer ventaId);

    @Query("SELECT c FROM ComprobanteElectronico c WHERE c.receptorNumDoc = :doc ORDER BY c.fechaRegistro DESC")
    List<ComprobanteElectronico> findByReceptorNumDoc(@Param("doc") String doc);
}
