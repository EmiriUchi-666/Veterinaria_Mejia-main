package com.Veterinaria.Mejia.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.Veterinaria.Mejia.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    
    // Spring Data JPA ya incluye el método count() por defecto, no necesitamos escribir JPQL para eso.
}