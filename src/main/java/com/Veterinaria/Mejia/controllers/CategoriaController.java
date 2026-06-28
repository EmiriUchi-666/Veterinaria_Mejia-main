package com.Veterinaria.Mejia.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.dto.CategoriaDTO;
import com.Veterinaria.Mejia.models.Categoria;
import com.Veterinaria.Mejia.repository.CategoriaRepository;
import com.Veterinaria.Mejia.services.CategoriaService;

@Controller
@RequestMapping("/mantenimiento/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaService categoriaService, CategoriaRepository categoriaRepository) {
        this.categoriaService = categoriaService;
        this.categoriaRepository = categoriaRepository;
    }

    // ==========================================
    // 1. PANTALLA PRINCIPAL: LISTAR CATEGORÍAS
    // ==========================================
    @GetMapping
    public String listarCategorias(Model model) {
        List<CategoriaDTO> categoriasConConteo = categoriaRepository.findAll().stream().map(cat -> {
            long conteo = categoriaRepository.contarProductosAsociadosJPQL(cat.getId());
            return new CategoriaDTO(cat.getId(), cat.getNombre(), conteo);
        }).collect(Collectors.toList());

        model.addAttribute("categorias", categoriasConConteo);
        return "mantenimiento/lista-categorias"; 
    }

    // ==========================================
    // 2. PANTALLA SECUNDARIA: FORMULARIO DE CREACIÓN
    // ==========================================
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "mantenimiento/form-categoria"; 
    }

    // ==========================================
    // 3. PROCESAR EL GUARDADO
    // ==========================================
    @PostMapping("/guardar")
    public String guardarCategoria(@ModelAttribute Categoria categoria, BindingResult result, 
                                   Model model, RedirectAttributes redirectAttrs) {
        
        // Validación de duplicados
        if (categoria.getNombre() != null && categoriaRepository.existsByNombre(categoria.getNombre().trim())) {
            result.rejectValue("nombre", "error.nombre", "🚨 Esta categoría ya se encuentra registrada en el catálogo.");
        }

        if (result.hasErrors()) {
            return "mantenimiento/form-categoria";
        }

        categoriaService.guardar(categoria);
        
        redirectAttrs.addFlashAttribute("successMsg", "Categoría '" + categoria.getNombre() + "' creada correctamente.");
        return "redirect:/mantenimiento/categorias";
    }

    // ==========================================
    // 4. ELIMINAR DE FORMA SEGURA
    // ==========================================
    @GetMapping("/eliminar/{id}")
    public String eliminarCategoria(@PathVariable("id") Integer id, RedirectAttributes redirectAttrs) {
        long productosAsociados = categoriaRepository.contarProductosAsociadosJPQL(id);
        
        // Regla estricta de seguridad contra inyecciones directas por URL
        if (productosAsociados > 0) {
            redirectAttrs.addFlashAttribute("errorMsg", "⚠️ Alerta: No se puede eliminar la categoría porque tiene " + productosAsociados + " productos amarrados.");
            return "redirect:/mantenimiento/categorias";
        }
        
        categoriaRepository.deleteById(id);
        redirectAttrs.addFlashAttribute("successMsg", "Clasificación eliminada con éxito del sistema maestro.");
        return "redirect:/mantenimiento/categorias";
    }
}