package com.Veterinaria.Mejia.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // CORRECCIÓN 1: Quitamos el "JPQL" del final del método
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no registrado: " + username));

        // CORRECCIÓN 2: Como ya guardamos "ROLE_Administrador" en la BD, lo usamos directo.
        // Si en tu BD lo guardaste solo como "Administrador" (sin el ROLE_ previo), 
        // entonces sí tendrías que dejarlo como: "ROLE_" + usuario.getRole().getNombreRol()
        String miRol = usuario.getRole().getNombreRol(); 
        
        SimpleGrantedAuthority autoridad = new SimpleGrantedAuthority(miRol);

        // Retornamos el objeto User nativo de Spring Security empaquetando todo de golpe
        return new User(
                usuario.getNombreUsuario(),
                usuario.getContrasena(),
                usuario.getEstado() != null && usuario.getEstado(), // isEnabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(autoridad)
        );
    }
}