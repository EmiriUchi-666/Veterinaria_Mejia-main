package com.Veterinaria.Mejia.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Categoria;
import com.Veterinaria.Mejia.models.Especie;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.repository.CategoriaRepository;
import com.Veterinaria.Mejia.repository.EspecieRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;
import com.Veterinaria.Mejia.services.CategoriaService;
import com.Veterinaria.Mejia.services.ProductoService;

@Controller
@RequestMapping("/almacen/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private EspecieRepository especieRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // ==========================================
    // 1. CATÁLOGO DE PRODUCTOS 
    // ==========================================
    @GetMapping
    public String listarProductosFiltrados(
            @RequestParam(value = "categoriaId", required = false) Integer categoriaId,
            @RequestParam(value = "especieId", required = false) Integer especieId,
            Model model) {
        
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("especies", especieRepository.findAll());
        
        model.addAttribute("categoriaSeleccionadaId", categoriaId);
        model.addAttribute("especieSeleccionadaId", especieId);

        if (categoriaId != null || especieId != null) {
            List<Producto> filtrados = productoRepository.findByCategoriaAndEspecieOptional(categoriaId, especieId);
            model.addAttribute("productos", filtrados);
        } else {
            model.addAttribute("productos", productoService.listarTodos());
        }

        return "almacen/lista-productos";
    }

    // ==========================================
    // 2. FORMULARIO PARA CREAR PRODUCTO
    // ==========================================
    @GetMapping("/nuevo")
    public String mostrarFormularioProducto(Model model) {
        // Creamos un producto y le asignamos el stock mínimo por defecto (12)
        Producto nuevoProducto = new Producto();
        
        model.addAttribute("producto", nuevoProducto);
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("especies", especieRepository.findAll());
        
        return "almacen/form-producto"; 
    }

    // ==========================================
    // 3. GUARDAR PRODUCTO (Aplica tus reglas)
    // ==========================================
    @PostMapping("/guardar")
    public String guardarNuevoProducto(@ModelAttribute Producto producto, RedirectAttributes redirectAttrs) {
        try {
            // Validar Categoría
            if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
                Categoria cat = categoriaRepository.findById(producto.getCategoria().getId()).orElseThrow();
                producto.setCategoria(cat);
            }
            // Validar Especie
            if (producto.getEspecie() != null && producto.getEspecie().getId() != null) {
                Especie esp = especieRepository.findById(producto.getEspecie().getId()).orElseThrow();
                producto.setEspecie(esp);
            }

            // Reglas de negocio forzadas:
            producto.setEstado(true); // Siempre activo al nacer
            
            // Si el usuario dejó el stock inicial vacío, lo ponemos en 0
            if (producto.getStockTotal() == null) {
                producto.setStockTotal(BigDecimal.ZERO);
            }

            productoService.guardarProductoNuevo(producto);
            redirectAttrs.addFlashAttribute("successMsg", "El producto se registró correctamente en el catálogo.");
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", "Error al guardar el producto. Revise los datos ingresados.");
            return "redirect:/almacen/productos/nuevo";
        }
        
        return "redirect:/almacen/productos";
    }

    // ==========================================
    // 4. DESECHAR PRODUCTO (Mermas / Pérdidas)
    // ==========================================
    @PostMapping("/desechar")
    public String registrarPerdida(
            @RequestParam("idProducto") Integer id,
            @RequestParam("cantidad") BigDecimal cantidad,
            RedirectAttributes redirectAttrs) {

        try {
            // ¡AQUÍ ESTÁ LA MAGIA! Llamamos a tu servicio que guarda en la tabla Mermas
            productoService.reportarMermaDesecho(id, cantidad);

            redirectAttrs.addFlashAttribute("successMsg", "Se ha registrado la pérdida y descontado del stock correctamente.");
            
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        
        return "redirect:/almacen/productos";
    }

    // ==========================================
    // 5. CAMBIAR ESTADO (Activo/Inactivo)
    // ==========================================
    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstadoProducto(@PathVariable("id") Integer id, RedirectAttributes redirectAttrs) {
        try {
            Producto prod = productoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("El producto solicitado no existe."));
            
            productoService.modificarEstado(id, !prod.getEstado());
            redirectAttrs.addFlashAttribute("successMsg", "Estado del producto actualizado.");
            
        } catch (RuntimeException e) { 
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/almacen/productos";
    }
}