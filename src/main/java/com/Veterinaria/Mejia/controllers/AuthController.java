package com.Veterinaria.Mejia.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.services.UsuarioService;

@Controller
public class AuthController {
    
    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        // Inyección por constructor: Es la mejor práctica recomendada por Spring
        this.usuarioService = usuarioService;
    }

    // ==========================================
    // 1. RUTAS DE INICIO DE SESIÓN
    // ==========================================

    @GetMapping("/")
    public String redireccionarALogin() {
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, 
                            @RequestParam(required = false) String success, 
                            Model model) {
        
        // Mensaje de error (Viene automático de Spring Security si falla)
        if (error != null) {
            model.addAttribute("errorMsg", "Credenciales inválidas o el usuario se encuentra inactivo.");
        }
        
        // Mensaje de éxito (Viene de nuestra recuperación de contraseña)
        if (success != null) {
            model.addAttribute("successMsg", "¡Contraseña actualizada con éxito! Por favor, inicia sesión.");
        }
        
        return "auth/login";
    }

    // ==========================================
    // 2. RUTAS DE RECUPERACIÓN DE CONTRASEÑA
    // ==========================================

    // Muestra el formulario HTML para recuperar la clave
    @GetMapping("/forgot-password")
    public String mostrarFormularioRecuperacion(Model model) {
        // Paso 1: Mostrar solo el campo de usuario
        model.addAttribute("paso", 1);
        return "auth/forgot-password";
    }

    // Busca al usuario y, si existe, muestra su pregunta secreta
    @PostMapping("/recuperar-password/buscar")
    public String buscarUsuarioParaRecuperacion(@RequestParam("nombreUsuario") String nombreUsuario, 
                                                Model model, 
                                                RedirectAttributes redirectAttributes) {
        return usuarioService.buscarUsuarioParaRecuperacion(nombreUsuario)
            .map(usuario -> {
                // Paso 2: Mostrar la pregunta y los campos para la nueva clave
                model.addAttribute("paso", 2);
                model.addAttribute("nombreUsuario", usuario.getNombreUsuario());
                model.addAttribute("preguntaSecreta", usuario.getPreguntaSecreta());
                return "auth/forgot-password";
            })
            .orElseGet(() -> {
                redirectAttributes.addFlashAttribute("errorMsg", "El nombre de usuario ingresado no existe.");
                return "redirect:/forgot-password";
            });
    }

    // Procesa el cambio de contraseña final
    @PostMapping("/reset-password")
    public String procesarRecuperacion(@RequestParam("nombreUsuario") String nombreUsuario,
                                       @RequestParam("respuestaSecreta") String respuestaSecreta,
                                       @RequestParam("nuevaContrasena") String nuevaContrasena,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        try {
            usuarioService.recuperarContrasenaPorPreguntaSecreta(nombreUsuario, respuestaSecreta, nuevaContrasena);
            
            redirectAttributes.addAttribute("success", "true");
            return "redirect:/login";
            
        } catch (RuntimeException e) { 
            // Si hay un error (respuesta incorrecta, clave débil, etc.), volvemos al paso 2
            model.addAttribute("errorMsg", e.getMessage());
            
            // Re-poblamos los datos para que el usuario no pierda el contexto
            usuarioService.buscarUsuarioParaRecuperacion(nombreUsuario).ifPresent(usuario -> {
                model.addAttribute("paso", 2);
                model.addAttribute("nombreUsuario", usuario.getNombreUsuario());
                model.addAttribute("preguntaSecreta", usuario.getPreguntaSecreta());
            });
            
            return "auth/forgot-password";
        }
    }
}