package com.Veterinaria.Mejia.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.Role;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Inyectamos el encriptador de contraseñas

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);


    // Regex de contraseña compartida para creación y actualización
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$";

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }

    // VERIFICAR SI EXISTE NOMBRE DE USUARIO (DNI) PARA VALIDACIÓN ASÍNCRONA (AJAX)
    public boolean existeNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario).isPresent();
    }

    // Método para el flujo de recuperación de contraseña, no lanza excepción si no lo encuentra
    public Optional<Usuario> buscarUsuarioParaRecuperacion(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario);
    }

    // REGISTRO DE NUEVO USUARIO
    @Transactional
    public Usuario guardarUsuarioNuevo(Usuario usuario) {
        log.info("Intentando registrar nuevo usuario: {}", usuario.getNombreUsuario());

        // 2. Validar que el DNI/Username no se repita
        Optional<Usuario> existeUsuario = usuarioRepository.findByNombreUsuario(usuario.getNombreUsuario());
        if (existeUsuario.isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
        }

        // 3. Validar la contraseña plana antes de encriptarla
        String contrasenaPlana = usuario.getContrasena();
        if (!contrasenaPlana.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial.");
        }

        // 4. Encriptar contraseña principal
        usuario.setContrasena(passwordEncoder.encode(contrasenaPlana));

        // 5. Validar que se haya seleccionado una pregunta secreta
        if (usuario.getPreguntaSecreta() == null || usuario.getPreguntaSecreta().trim().isEmpty()) {
            throw new IllegalArgumentException("La pregunta secreta es obligatoria para el registro.");
        }

        // 6. Encriptar la respuesta secreta (Pasamos a minúsculas para evitar errores de tipeo al recuperar)
        if (usuario.getRespuestaSecreta() != null && !usuario.getRespuestaSecreta().trim().isEmpty()) {
            String respuestaNormalizada = usuario.getRespuestaSecreta().trim().toLowerCase();
            usuario.setRespuestaSecreta(passwordEncoder.encode(respuestaNormalizada));
        } else {
            throw new IllegalArgumentException("La respuesta secreta es obligatoria para el registro.");
        }

        usuario.setEstado(true); // Siempre nace activo
        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario {} registrado exitosamente con rol {}", guardado.getNombreUsuario(), guardado.getRole().getNombreRol());
        return guardado;
    }

    /// MODIFICAR ESTADO (Para bloquear el acceso al sistema)
    @Transactional
    public void modificarEstado(Integer idUsuario, boolean nuevoEstado) {
        Usuario usuario = buscarPorId(idUsuario);
        
        // Blindaje: Evitar que el dueño se desactive a sí mismo por error si es el único administrador
        // CORRECCIÓN: Contar específicamente los administradores activos, no todos los usuarios del sistema.
        long adminCount = listarTodos().stream()
                .filter(u -> u.getRole() != null && "ROLE_Administrador".equals(u.getRole().getNombreRol()) && u.isEstado())
                .count();
                
        if (!nuevoEstado && "ROLE_Administrador".equals(usuario.getRole().getNombreRol()) && usuario.isEstado() && adminCount <= 1) {
             throw new IllegalArgumentException("Bloqueo de seguridad: No puedes desactivar al único administrador del sistema.");
        }
        
        usuario.setEstado(nuevoEstado);
        log.info("Estado del usuario {} cambiado a: {}", usuario.getNombreUsuario(), nuevoEstado ? "Activo" : "Inactivo");
        usuarioRepository.save(usuario);
    }

    // CAMBIAR ROL DEL USUARIO
    @Transactional
    public void cambiarRol(Integer idUsuario, Role nuevoRol) {
        Usuario usuario = buscarPorId(idUsuario);
        usuario.setRole(nuevoRol); 
        log.info("Rol del usuario {} cambiado a: {}", usuario.getNombreUsuario(), nuevoRol.getNombreRol());
        usuarioRepository.save(usuario);
    }

    // CAMBIAR CONTRASEÑA MANUAL (Estando logueado dentro del sistema)
    @Transactional
    public void cambiarContrasena(Integer idUsuario, String nuevaContrasenaPlana) {
        Usuario usuario = buscarPorId(idUsuario);
        ejecutarActualizacionDeClave(usuario, nuevaContrasenaPlana);
    }

    // =========================================================================
    // RECUPERACIÓN DE CONTRASEÑA POR PREGUNTA SECRETA (Olvidó la clave)
    // =========================================================================
    @Transactional
    public void recuperarContrasenaPorPreguntaSecreta(String nombreUsuario, String respuestaDigitada, String nuevaContrasenaPlana) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("El usuario ingresado no existe en el sistema."));

        // Normalizar la respuesta tipeada (a minúsculas) y comparar con el Hash de la BD
        String respuestaNormalizada = respuestaDigitada.trim().toLowerCase();
        
        if (!passwordEncoder.matches(respuestaNormalizada, usuario.getRespuestaSecreta())) {
            throw new IllegalArgumentException("La respuesta secreta es incorrecta. Acceso denegado.");
        }

        // Si la respuesta coincide, se autoriza el cambio de clave
        ejecutarActualizacionDeClave(usuario, nuevaContrasenaPlana);
    }

    // =========================================================================
    // MÉTODO PRIVADO: Para no repetir el código de validación en los 2 métodos de arriba
    // =========================================================================
    private void ejecutarActualizacionDeClave(Usuario usuario, String nuevaContrasenaPlana) {
        if (!nuevaContrasenaPlana.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException("La nueva contraseña no cumple con los requisitos de seguridad.");
        }

        if (passwordEncoder.matches(nuevaContrasenaPlana, usuario.getContrasena())) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la anterior por motivos de seguridad.");
        }

        usuario.setContrasena(passwordEncoder.encode(nuevaContrasenaPlana));
        usuarioRepository.save(usuario);
        log.info("Contraseña actualizada para el usuario {}", usuario.getNombreUsuario());
    }
}