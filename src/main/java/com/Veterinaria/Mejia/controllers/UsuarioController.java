package com.Veterinaria.Mejia.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Role;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.RoleRepository;
import com.Veterinaria.Mejia.services.UsuarioService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RoleRepository roleRepository;

    // ==========================================
    // 1. PANEL DE GESTIÓN DE USUARIOS
    // ==========================================
    @GetMapping
    public String listarUsuarios(Model model, Principal principal) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("roles", roleRepository.findAll()); // Para los modales de cambio de rol
        
        // Identificamos quién está logueado actualmente para bloquear sus propios botones de edición
        if (principal != null) {
            model.addAttribute("loggedUsername", principal.getName());
        }
        
        return "usuarios/gestion-usuarios";
    }

    // ==========================================
    // 2. FORMULARIO DE NUEVO USUARIO
    // ==========================================
    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(Model model) {
        // 1. Enviamos un usuario vacío para que el formulario lo llene
        model.addAttribute("usuario", new Usuario());
        
        // 2. Enviamos la lista de roles desde la base de datos para el menú desplegable
        model.addAttribute("roles", roleRepository.findAll()); 
        
        return "usuarios/form-usuario"; // Aquí debe coincidir con el nombre del archivo que creamos arriba
    }

    // ==========================================
    // 3. GUARDAR USUARIO (Atrapando tus reglas de seguridad)
    // ==========================================
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttrs) {
        try {
            usuarioService.guardarUsuarioNuevo(usuario);
            redirectAttrs.addFlashAttribute("successMsg", "El usuario '" + usuario.getNombreUsuario() + "' ha sido registrado con éxito.");
            return "redirect:/usuarios";
            
        } catch (RuntimeException e) {
            // Atrapa la validación de los 5 usuarios, la contraseña débil o el nombre duplicado
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
            
            // Lo devolvemos al formulario para que intente de nuevo
            return "redirect:/usuarios/nuevo";
        }
    }

    // ==========================================
    // 4. BLOQUEAR / DESBLOQUEAR ACCESO
    // ==========================================
    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstadoUsuario(@PathVariable("id") Integer id, Principal principal, jakarta.servlet.http.HttpSession session, RedirectAttributes redirectAttrs) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            
            // Protección en el Backend: Expulsar si intenta bloquearse a sí mismo maliciosamente
            if (principal != null && usuario.getNombreUsuario().equals(principal.getName())) {
                session.invalidate(); // Destruye la sesión actual inmediatamente
                return "redirect:/login?error=true";
            }
            
            // Invertimos el estado actual (Si era true, pasa a false)
            usuarioService.modificarEstado(id, !usuario.isEstado());
            
            String accion = !usuario.isEstado() ? "desactivado" : "activado"; // Mensaje dinámico
            redirectAttrs.addFlashAttribute("successMsg", "El usuario ha sido " + accion + " correctamente.");
            
        } catch (RuntimeException e) {
            // Atrapa tu regla de "No puedes desactivar al único administrador"
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        
        return "redirect:/usuarios";
    }

    // ==========================================
    // 5. CAMBIAR ROL (Desde un select/modal en la lista)
    // ==========================================
    @PostMapping("/cambiar-rol")
    public String cambiarRolUsuario(@RequestParam("idUsuario") Integer idUsuario, 
                                    @RequestParam("idRol") Integer idRol, 
                                    Principal principal, jakarta.servlet.http.HttpSession session,
                                    RedirectAttributes redirectAttrs) {
        try {
            Usuario usuario = usuarioService.buscarPorId(idUsuario);
            
            // Protección en el Backend: Expulsar si intenta cambiarse el rol a sí mismo
            if (principal != null && usuario.getNombreUsuario().equals(principal.getName())) {
                session.invalidate();
                return "redirect:/login?error=true";
            }
            
            Role nuevoRol = roleRepository.findById(idRol)
                    .orElseThrow(() -> new RuntimeException("El rol seleccionado no existe."));
            
            usuarioService.cambiarRol(idUsuario, nuevoRol);
            redirectAttrs.addFlashAttribute("successMsg", "Los permisos del usuario han sido actualizados.");
            
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        
        return "redirect:/usuarios";
    }
    
    // ==========================================
    // 6. CAMBIAR CONTRASEÑA DIRECTAMENTE
    // ==========================================
    @PostMapping("/cambiar-password")
    public String cambiarPasswordAdmin(@RequestParam("idUsuario") Integer idUsuario,
                                       @RequestParam("nuevaContrasena") String nuevaContrasena,
                                       Principal principal, jakarta.servlet.http.HttpSession session,
                                       RedirectAttributes redirectAttrs) {
        try {
            Usuario usuario = usuarioService.buscarPorId(idUsuario);
            
            // Protección en el Backend: Expulsar si intenta cambiarse la clave saltándose la interfaz
            if (principal != null && usuario.getNombreUsuario().equals(principal.getName())) {
                session.invalidate();
                return "redirect:/login?error=true";
            }
            
            usuarioService.cambiarContrasena(idUsuario, nuevaContrasena);
            redirectAttrs.addFlashAttribute("successMsg", "La contraseña ha sido actualizada exitosamente por el administrador.");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    // ==========================================
    // 7. ENDPOINT AJAX PARA VERIFICAR SI USUARIO YA EXISTE
    // ==========================================
    @GetMapping("/verificar-username")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verificarUsername(@RequestParam("username") String username) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", usuarioService.existeNombreUsuario(username));
        return ResponseEntity.ok(response);
    }
}