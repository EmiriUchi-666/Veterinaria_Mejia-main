package com.Veterinaria.Mejia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Veterinaria.Mejia.models.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    @Query("SELECT c FROM Categoria c WHERE c.nombre LIKE %:nombre%")
    List<Categoria> buscarPorNombreJPQL(@Param("nombre") String nombre);

    boolean existsByNombre(String nombre);

    // 🚨 NUEVA QUERY: Cuenta cuántos productos usan esta categoría exacta de forma interactiva
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId")
    long contarProductosAsociadosJPQL(@Param("categoriaId") Integer categoriaId);
}