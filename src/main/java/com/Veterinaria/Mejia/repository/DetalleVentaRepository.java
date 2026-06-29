package com.Veterinaria.Mejia.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.Veterinaria.Mejia.dto.TopProductoDTO;
import com.Veterinaria.Mejia.models.DetalleVenta;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    @Query("SELECT d FROM DetalleVenta d WHERE d.venta.id = :ventaId")
    List<DetalleVenta> buscarDetallesPorVentaJPQL(Integer ventaId);

    // =========================================================================
    // CORRECCIÓN: Nombre restaurado para evitar el error "undefined method"
    // =========================================================================
    @Query("SELECT d.producto.nombre AS nombreProducto, SUM(d.cantidad) AS totalVendido " +
           "FROM DetalleVenta d WHERE d.venta.fechaEmision >= :inicio AND d.producto IS NOT NULL " +
           "GROUP BY d.producto.id ORDER BY SUM(d.cantidad) DESC")
    List<TopProductoDTO> findTop10ProductosVendidos(java.time.LocalDateTime inicio, Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.cantidad * d.producto.precioInversion), 0) " +
           "FROM DetalleVenta d WHERE d.venta.fechaEmision >= :inicio AND d.producto IS NOT NULL")
    BigDecimal calcularInversionDeVentasJPQL(java.time.LocalDateTime inicio);

    /**
     * FASE 7: Calcula la rentabilidad por servicio.
     * Agrupa los detalles de venta por servicio y suma los subtotales.
     */
    @Query("SELECT new com.Veterinaria.Mejia.dto.ServicioRentabilidadDTO(" +
           "s.nombreServicio, SUM(d.subtotal)) " +
           "FROM DetalleVenta d JOIN d.servicio s " +
           "WHERE d.venta.estado = true AND d.venta.fechaEmision BETWEEN :inicio AND :fin " +
           "GROUP BY s.id, s.nombreServicio " +
           "ORDER BY SUM(d.subtotal) DESC")
    List<com.Veterinaria.Mejia.dto.ServicioRentabilidadDTO> obtenerRentabilidadServicios(java.time.LocalDateTime inicio, java.time.LocalDateTime fin);

    /**
     * FASE 7: Calcula la rentabilidad por producto.
     * Agrupa los detalles de venta por producto y calcula los totales.
     */
    @Query("SELECT new com.Veterinaria.Mejia.dto.ProductoRentabilidadDTO(" +
           "p.nombre, " +
           "SUM(d.cantidad), " +
           "p.precioInversion, " +
           "d.precioUnitario, " +
           "SUM(d.subtotal), " +
           "SUM(d.cantidad * p.precioInversion), " +
           "SUM(d.subtotal) - SUM(d.cantidad * p.precioInversion)) " +
           "FROM DetalleVenta d JOIN d.producto p " +
           "WHERE d.venta.estado = true AND d.venta.fechaEmision BETWEEN :inicio AND :fin " +
           "GROUP BY p.id, p.nombre, p.precioInversion, d.precioUnitario ORDER BY SUM(d.subtotal) DESC")
    List<com.Veterinaria.Mejia.dto.ProductoRentabilidadDTO> obtenerRentabilidadProductos(java.time.LocalDateTime inicio, java.time.LocalDateTime fin);
}