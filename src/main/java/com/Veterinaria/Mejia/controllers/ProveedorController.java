package com.Veterinaria.Mejia.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Proveedor;
import com.Veterinaria.Mejia.repository.ProveedorRepository;
import com.Veterinaria.Mejia.services.ProveedorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/almacen/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;
    private final ProveedorRepository proveedorRepository;

    // Inyección por constructor (Recomendada)
    public ProveedorController(ProveedorService proveedorService, ProveedorRepository proveedorRepository) {
        this.proveedorService = proveedorService;
        this.proveedorRepository = proveedorRepository;
    }

    // ==========================================
    // 1. LISTAR PROVEEDORES
    // ==========================================
    @GetMapping
    public String listarProveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.listarTodos());
        return "almacen/lista-proveedores";
    }

    // ==========================================
    // 2. MOSTRAR FORMULARIO
    // ==========================================
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "almacen/form-proveedor";
    }

    // ==========================================
    // 3. GUARDAR Y VALIDAR
    // ==========================================
    @PostMapping("/guardar")
    public String guardarProveedor(@Valid @ModelAttribute("proveedor") Proveedor proveedor, 
                                   BindingResult result, Model model, RedirectAttributes redirectAttrs) {
        
        // 1. VALIDACIÓN CONTROLADA: Verifica duplicación mediante tu query JPQL
        if (proveedor.getRuc() != null && proveedorRepository.buscarPorRucJPQL(proveedor.getRuc()).isPresent()) {
            result.rejectValue("ruc", "error.ruc", "🚨 Ya existe un proveedor registrado con este número de RUC.");
        }

        // 2. Si hay errores o duplicados, lo devolvemos a su propio formulario limpio
        if (result.hasErrors()) {
            return "almacen/form-proveedor";
        }
        
        proveedorService.guardar(proveedor);
        
        // 3. Redirección limpia al directorio con parámetro de éxito oculto
        redirectAttrs.addFlashAttribute("successMsg", "Proveedor '" + proveedor.getNombreProveedor() + "' registrado correctamente.");
        return "redirect:/almacen/proveedores";
    }
}