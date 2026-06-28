package com.Veterinaria.Mejia.services;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    // Ideal para validaciones manuales o APIs REST (Login vía JSON)
    public Optional<Usuario> login(String nombreUsuario, String contrasenaRaw) {
        // CORRECCIÓN: Usamos findByNombreUsuario (sin el JPQL al final)
        return repo.findByNombreUsuario(nombreUsuario)
            // encoder.matches() compara la clave plana que el usuario escribe con el Hash de la BD
            .filter(u -> encoder.matches(contrasenaRaw, u.getContrasena()));
    }
}