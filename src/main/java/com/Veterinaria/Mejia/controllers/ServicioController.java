package com.Veterinaria.Mejia.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Servicio;
import com.Veterinaria.Mejia.services.ServicioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mantenimiento/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    // ==========================================
    // 1. LISTAR TODOS LOS SERVICIOS (Activos e Inactivos)
    // ==========================================
    @GetMapping
    public String listarServicios(Model model) {
        // CAMBIADO: Usamos buscarServicios(null) para traer TODOS, 
        // así podemos ver los inactivos y volver a activarlos si queremos.
        model.addAttribute("servicios", servicioService.buscarServicios(null));
        return "mantenimiento/lista-servicios";
    }

    // ==========================================
    // 2. MOSTRAR FORMULARIO (NUEVO)
    // ==========================================
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("servicio", new Servicio());
        return "mantenimiento/form-servicio";
    }

    // ==========================================
    // 3. GUARDAR (NUEVO O ACTUALIZADO)
    // ==========================================
    @PostMapping("/guardar")
    public String guardarServicio(@Valid @ModelAttribute("servicio") Servicio servicio, 
                                  BindingResult result,
                                  RedirectAttributes redirectAttrs) {
        
        if (result.hasErrors()) {
            return "mantenimiento/form-servicio";
        }
        
        // Si el ID es nulo, es nuevo y nacerá activo (gracias a tu ServicioService).
        // Si tiene ID, es una edición y mantendrá su estado.
        servicioService.guardarServicioNuevo(servicio);
        
        redirectAttrs.addFlashAttribute("successMsg", "El servicio ha sido guardado exitosamente en el tarifario.");
        return "redirect:/mantenimiento/servicios";
    }

    // ==========================================
    // 4. MOSTRAR FORMULARIO (EDITAR) -> ¡NUEVA RUTA!
    // ==========================================
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model, RedirectAttributes redirectAttrs) {
        try {
            Servicio servicioExistente = servicioService.buscarPorId(id);
            model.addAttribute("servicio", servicioExistente);
            return "mantenimiento/form-servicio"; // Reutilizamos el mismo HTML
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMsg", "Error: El servicio que intenta editar no existe.");
            return "redirect:/mantenimiento/servicios";
        }
    }

    // ==========================================
    // 5. CAMBIAR ESTADO (Borrado Lógico) -> ¡NUEVA RUTA!
    // ==========================================
    @GetMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Integer id, RedirectAttributes redirectAttrs) {
        try {
            Servicio servicio = servicioService.buscarPorId(id);
            // Invertimos el estado: Si está true (activo) pasa a false (inactivo), y viceversa.
            boolean nuevoEstado = !servicio.getEstado();
            
            // Usamos tu método del Service
            servicioService.modificarEstado(id, nuevoEstado);
            
            String accion = nuevoEstado ? "restaurado y activado" : "desactivado y ocultado";
            redirectAttrs.addFlashAttribute("successMsg", "El servicio ha sido " + accion + " correctamente.");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMsg", "Error al intentar cambiar el estado del servicio.");
        }
        return "redirect:/mantenimiento/servicios";
    }

    // ==========================================
    // 6. ELIMINAR SERVICIO (Borrado Físico)
    // ==========================================
    @GetMapping("/eliminar/{id}")
    public String eliminarServicio(@PathVariable Integer id, RedirectAttributes redirectAttrs) {
        try {
            servicioService.eliminarFisicamente(id);
            redirectAttrs.addFlashAttribute("successMsg", "El servicio ha sido eliminado por completo del sistema.");
        } catch (RuntimeException e) {
            // Captura el mensaje de la excepción si ya fue vendido
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/mantenimiento/servicios";
    }
}