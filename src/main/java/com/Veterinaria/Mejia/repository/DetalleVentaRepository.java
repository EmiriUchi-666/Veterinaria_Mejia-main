package com.Veterinaria.Mejia.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Veterinaria.Mejia.dto.TopProductoDTO;
import com.Veterinaria.Mejia.models.DetalleVenta;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    @Query("SELECT d FROM DetalleVenta d WHERE d.venta.id = :ventaId")
    List<DetalleVenta> buscarDetallesPorVentaJPQL(@Param("ventaId") Integer ventaId);

    // =========================================================================
    // CORRECCIÓN: Nombre restaurado para evitar el error "undefined method"
    // =========================================================================
    @Query("SELECT d.producto.nombre AS nombreProducto, SUM(d.cantidad) AS totalVendido " +
           "FROM DetalleVenta d WHERE d.venta.fechaEmision >= :inicio AND d.producto IS NOT NULL " +
           "GROUP BY d.producto.id ORDER BY SUM(d.cantidad) DESC")
    List<TopProductoDTO> findTop10ProductosVendidos(@Param("inicio") LocalDateTime inicio, Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.cantidad * d.producto.precioInversion), 0) " +
           "FROM DetalleVenta d WHERE d.venta.fechaEmision >= :inicio AND d.producto IS NOT NULL")
    BigDecimal calcularInversionDeVentasJPQL(@Param("inicio") LocalDateTime inicio);
}