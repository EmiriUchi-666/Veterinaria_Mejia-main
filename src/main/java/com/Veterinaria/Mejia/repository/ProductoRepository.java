package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Veterinaria.Mejia.models.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * Busca productos de forma dinámica. Si una de las IDs es nula,
     * la ignora en la búsqueda, permitiendo filtrar solo por categoría,
     * solo por especie, o por ambas.
     */
    @Query("SELECT p FROM Producto p WHERE (:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND (:especieId IS NULL OR p.especie.id = :especieId)")
    List<Producto> findByCategoriaAndEspecieOptional(@Param("categoriaId") Integer categoriaId, @Param("especieId") Integer especieId);

    @Query("SELECT p FROM Producto p WHERE " +
           "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
           "(:especieId IS NULL OR p.especie.id = :especieId)")
    List<Producto> buscarYFiltrarInventarioJPQL(@Param("nombre") String nombre, @Param("categoriaId") Integer categoriaId, @Param("especieId") Integer especieId);

    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId AND p.especie.id = :especieId")
    List<Producto> buscarPorCategoriaYEspecieJPQL(@Param("categoriaId") Integer categoriaId, @Param("especieId") Integer especieId);

    /**
     * Devuelve los productos cuyo stock de unidades cerradas (sacos/cajas)
     * es menor o igual al umbral mínimo definido en la entidad.
     */
    @Query("SELECT p FROM Producto p WHERE p.stockCerrado <= p.stockMinimo")
    List<Producto> buscarProductosStockCriticoJPQL();

    /**
     * Devuelve todos los productos activos (estado = true), sin distinción
     * de uso clínico, ya que ese concepto fue retirado del sistema.
     */
    List<Producto> findByEstadoTrue();
    
    /**
     * Devuelve todos los productos activos (estado = true) que NO son alimentos (esAlimento = false).
     * Útil para listar productos que pueden ser usados en tratamientos clínicos.
     */
    List<Producto> findByEstadoTrueAndEsAlimentoFalse();

       // A6 – Optimistic/Pessimistic locking para evitar stock negativo en concurrencia
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Producto p WHERE p.id = :id")
    java.util.Optional<com.Veterinaria.Mejia.models.Producto> findByIdWithLock(
        @org.springframework.data.repository.query.Param("id") Integer id);

    List<Producto> findByCategoriaNombre(String nombre);

    /**
     * FASE 8: Alerta de Baja Rotación.
     * Devuelve productos que no han tenido ninguna venta desde la fecha límite especificada.
     */
    @Query("SELECT p FROM Producto p WHERE p.id NOT IN (SELECT DISTINCT d.producto.id FROM DetalleVenta d WHERE d.producto IS NOT NULL AND d.venta.fechaEmision >= :fechaLimite)")
    List<Producto> findProductosSinVentasDesde(@Param("fechaLimite") java.time.LocalDateTime fechaLimite);
}
