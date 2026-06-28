package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Veterinaria.Mejia.models.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    /**
     * Busca productos de forma dinámica. Si una de las IDs es nula,
     * la ignora en la búsqueda, permitiendo filtrar solo por categoría,
     * solo por especie, o por ambas.
     */
    @Query("SELECT p FROM Producto p WHERE (:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND (:especieId IS NULL OR p.especie.id = :especieId)")
    List<Producto> findByCategoriaAndEspecieOptional(@Param("categoriaId") Integer categoriaId, @Param("especieId") Integer especieId);

    @Query("SELECT p FROM Producto p WHERE " +
           "(:nombre IS NULL OR p.nombre LIKE %:nombre%) AND " +
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

}
    // A6 – Optimistic locking para evitar stock negativo en concurrencia
    @org.springframework.data.jpa.repository.Query(
        value = "SELECT p FROM Producto p WHERE p.id = :id",
        lockMode = jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<com.Veterinaria.Mejia.models.Producto> findByIdWithLock(
        @org.springframework.data.repository.query.Param("id") Integer id);
