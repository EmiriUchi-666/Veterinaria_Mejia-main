package com.Veterinaria.Mejia.repository;

import com.Veterinaria.Mejia.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    // JPQL: Recupera los roles del sistema ordenados por jerarquía interna
    @Query("SELECT r FROM Role r ORDER BY r.id ASC")
    List<Role> obtenerRolesSistemaJPQL();
}