package com.Veterinaria.Mejia.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.IngresoStock;
import com.Veterinaria.Mejia.services.CategoriaService;
import com.Veterinaria.Mejia.services.EspecieService;
import com.Veterinaria.Mejia.services.IngresoStockService;
import com.Veterinaria.Mejia.services.ProductoService;
import com.Veterinaria.Mejia.services.ProveedorService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/almacen/ingresos")
@RequiredArgsConstructor
public class IngresoStockController {

    private final IngresoStockService ingresoStockService;
    private final ProductoService productoService;
    private final ProveedorService proveedorService;
    private final CategoriaService categoriaService;
    private final EspecieService especieService;

    // ==========================================
    // 1. HISTORIAL DE INGRESOS (Auditoría)
    // ==========================================
    @GetMapping
    public String listarHistorial(Model model) {
        model.addAttribute("ingresos", ingresoStockService.listarTodos());
        return "almacen/historial-ingresos";
    }

    // ==========================================
    // 2. PANEL DE ACTUALIZACIÓN DE STOCK (Formulario)
    // ==========================================
    @GetMapping("/nuevo")
    public String mostrarPanelStock(Model model) {
        // 1. Mandamos la cabecera vacía para que Thymeleaf la construya
        model.addAttribute("ingresoStock", new IngresoStock());
        
        // 2. Mandamos los catálogos para llenar los <select> de la pantalla
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("proveedores", proveedorService.listarTodos());
        
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("especies", especieService.listarTodas());
        
        return "almacen/panel-actualizacion-stock";
    }

    // ==========================================
    // 3. PROCESAR LA ENTRADA AL ALMACÉN
    // ==========================================
    @PostMapping("/guardar")
    public String guardarIngresoStock(@ModelAttribute IngresoStock ingresoStock, 
                                      RedirectAttributes redirectAttrs) {
        try {
            // El servicio (que ya programamos) se encargará de extraer los detalles,
            // validar las cantidades y sumar el stock físico a cada producto automáticamente.
            ingresoStockService.registrarIngresoMercaderia(ingresoStock);
            
            redirectAttrs.addFlashAttribute("successMsg", "¡Stock actualizado y lote registrado con éxito!");
            return "redirect:/almacen/ingresos";
            
        } catch (RuntimeException e) {
            // Si salta tu regla de negocio (ej. intentar meter más de 99 unidades)
            // atrapamos el error y lo devolvemos a la pantalla sin romper el programa.
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/almacen/ingresos/nuevo";
        }
    }
}