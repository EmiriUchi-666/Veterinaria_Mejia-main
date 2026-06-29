package com.Veterinaria.Mejia.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Veterinaria.Mejia.dto.VentasDiaDTO;
import com.Veterinaria.Mejia.models.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    // =========================================================================
    // 1. MÉTODOS ORIGINALES (Módulo de Ventas / Facturación)
    // =========================================================================

    // JPQL: Obtiene el correlativo máximo actual de la serie solicitada
    @Query("SELECT MAX(v.correlativo) FROM Venta v WHERE v.serie = :serie")
    Integer obtenerMaximoCorrelativoJPQL(String serie);

    @Query("SELECT COALESCE(MAX(nc.correlativo), 0) FROM NotaCredito nc WHERE nc.serie = :serie")
    Integer obtenerMaximoCorrelativoNotaCredito(String serie);

    // JPQL: Historial de ventas de un cliente específico
    @Query("SELECT v FROM Venta v WHERE v.cliente.id = :clienteId ORDER BY v.fechaEmision DESC")
    List<Venta> buscarHistorialPorClienteJPQL(Integer clienteId);

    // JPQL: Cuenta las ventas cuya fecha de emisión coincida con el día de hoy
    @Query("SELECT COUNT(v) FROM Venta v WHERE CAST(v.fechaEmision AS date) = CURRENT_DATE")
    long contarVentasDeHoyJPQL();

    // Historial General (Las últimas 10 ventas del sistema)
    List<Venta> findTop10ByOrderByFechaEmisionDesc();

    /**
     * FASE 5: Solución al problema N+1.
     * Carga las ventas y sus colecciones asociadas (detalles, productos, servicios) en una sola consulta.
     */
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.detallesVentas d LEFT JOIN FETCH d.producto LEFT JOIN FETCH d.servicio ORDER BY v.fechaEmision DESC")
    List<Venta> findTop10WithDetails(Pageable pageable);

    // Suma total de ingresos en un rango de fechas
    @Query("SELECT COALESCE(SUM(v.totalVenta), 0) FROM Venta v WHERE v.fechaEmision >= :inicio AND v.fechaEmision <= :fin")
    BigDecimal sumarIngresosPorFechaJPQL(java.time.LocalDateTime inicio, java.time.LocalDateTime fin);

    // Cuenta el número de ventas en un rango de fechas
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaEmision >= :inicio AND v.fechaEmision <= :fin")
    long contarVentasPorFechaJPQL(java.time.LocalDateTime inicio, java.time.LocalDateTime fin);

    // Gráfico: Agrupa las ventas por día y suma los montos
    @Query("SELECT DATE(v.fechaEmision) AS fecha, COALESCE(SUM(v.totalVenta), 0) AS totalMonto " +
           "FROM Venta v WHERE v.fechaEmision >= :inicio AND v.fechaEmision <= :fin " +
           "GROUP BY DATE(v.fechaEmision) ORDER BY DATE(v.fechaEmision) ASC")
    List<VentasDiaDTO> obtenerGraficoVentasPorFechaJPQL(java.time.LocalDateTime inicio, java.time.LocalDateTime fin);


    // =========================================================================
    // 2. NUEVOS MÉTODOS AÑADIDOS (Módulo de Reportes / Dashboard)
    // =========================================================================

    // KPI: Contar cantidad de ventas desde una fecha (con estado activo)
    // Trae la lista completa de ventas de un periodo que no estén anuladas (estado = true)
    List<Venta> findByFechaEmisionAfterAndEstado(java.time.LocalDateTime fecha, boolean estado);

    // KPI: Sumar ingresos brutos desde una fecha
    @Query("SELECT COALESCE(SUM(v.totalVenta), 0) FROM Venta v WHERE v.fechaEmision >= :inicio AND v.estado = true")
    BigDecimal sumarTotalVentasPorFecha(java.time.LocalDateTime inicio);

    // GRÁFICO: Agrupar por HORA (Para el reporte dinámico de "Hoy")
    @Query(value = "SELECT DATE_FORMAT(fecha_emision, '%H:00') AS fecha, SUM(total_venta) AS totalMonto " +
                   "FROM ventas WHERE fecha_emision >= :inicio AND estado = 1 " +
                   "GROUP BY HOUR(fecha_emision) ORDER BY HOUR(fecha_emision)", nativeQuery = true)
    List<VentasDiaDTO> obtenerVentasPorHora(java.time.LocalDateTime inicio);

    // GRÁFICO: Agrupar por DÍA (Para el reporte dinámico de "Semana" o "Mes")
    @Query(value = "SELECT DATE_FORMAT(fecha_emision, '%Y-%m-%d') AS fecha, SUM(total_venta) AS totalMonto " +
                   "FROM ventas WHERE fecha_emision >= :inicio AND estado = 1 " +
                   "GROUP BY DATE(fecha_emision) ORDER BY DATE(fecha_emision)", nativeQuery = true)
    List<VentasDiaDTO> obtenerVentasPorDia(java.time.LocalDateTime inicio);

    // =========================================================================
    // 3. CONSULTAS PARA LOS TOP 10 (Productos y Servicios)
    // =========================================================================

    /**
     * Proyección (Interface) para mapear automáticamente las sumas de las consultas.
     * Spring Data interceptará las columnas 'nombre', 'cantidad' y 'ganancia' y creará el objeto.
     */
    public interface TopItemProjection {
        String getNombre();
        BigDecimal getCantidad();
        BigDecimal getGanancia();
    }

    // TOP Productos Más Vendidos
    @Query("SELECT d.producto.nombre AS nombre, SUM(d.cantidad) AS cantidad, SUM(d.subtotal) AS ganancia " +
           "FROM Venta v JOIN v.detallesVentas d " +
           "WHERE v.fechaEmision >= :inicio AND v.fechaEmision <= :fin AND v.estado = true " +
           "AND d.producto IS NOT NULL " +
           "GROUP BY d.producto.id, d.producto.nombre " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<TopItemProjection> obtenerTopProductosPorFechaJPQL(java.time.LocalDateTime inicio, java.time.LocalDateTime fin, Pageable pageable);

    // TOP Servicios Más Solicitados
    @Query("SELECT d.servicio.nombreServicio AS nombre, SUM(d.cantidad) AS cantidad, SUM(d.subtotal) AS ganancia " +
           "FROM Venta v JOIN v.detallesVentas d " +
           "WHERE v.fechaEmision >= :inicio AND v.fechaEmision <= :fin AND v.estado = true " +
           "AND d.servicio IS NOT NULL " +
           "GROUP BY d.servicio.id, d.servicio.nombreServicio " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<TopItemProjection> obtenerTopServiciosPorFechaJPQL(java.time.LocalDateTime inicio, java.time.LocalDateTime fin, Pageable pageable);
}