package com.Veterinaria.Mejia.controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Proveedor;
import com.Veterinaria.Mejia.repository.ProveedorRepository;
import com.Veterinaria.Mejia.services.ProveedorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/almacen/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

 private final ProveedorRepository proveedorRepository;
 private final ProveedorService proveedorService;

    @GetMapping
    public String listar(Model model) {
 model.addAttribute("proveedores", proveedorService.listarTodos());
        return "almacen/lista-proveedores";
    }

    @GetMapping("/nuevo")
    public String formNuevo(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "almacen/form-proveedor";
    }

    @GetMapping("/editar/{id}")
    public String formEditar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
 Proveedor proveedor = proveedorService.buscarPorId(id);
        if (proveedor == null) {
            ra.addFlashAttribute("errorMsg", "Proveedor no encontrado.");
            return "redirect:/almacen/proveedores";
        }
        model.addAttribute("proveedor", proveedor);
        return "almacen/form-proveedor";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("proveedor") Proveedor proveedor, BindingResult result, RedirectAttributes ra) {
        // Validación de RUC duplicado
        if (proveedor.getRuc() != null) {
            Optional<Proveedor> existente = proveedorRepository.buscarPorRucJPQL(proveedor.getRuc());
            if (existente.isPresent() && !existente.get().getId().equals(proveedor.getId())) {
                result.rejectValue("ruc", "error.ruc", "Ya existe otro proveedor con este RUC.");
            }
        }

        if (result.hasErrors()) {
            return "almacen/form-proveedor";
        }

        // Si el estado es nulo (porque el checkbox no se envió), lo ponemos en true por defecto
        if (proveedor.getEstado() == null) {
            proveedor.setEstado(true);
        }

        try {
 proveedorService.guardar(proveedor);
            ra.addFlashAttribute("successMsg", "Proveedor '" + proveedor.getNombreProveedor() + "' guardado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al guardar el proveedor: " + e.getMessage());
        }
        return "redirect:/almacen/proveedores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        // Aquí se podría añadir una validación para no eliminar si tiene ingresos asociados
 proveedorService.eliminar(id);
        ra.addFlashAttribute("successMsg", "Proveedor eliminado correctamente.");
        return "redirect:/almacen/proveedores";
    }
}