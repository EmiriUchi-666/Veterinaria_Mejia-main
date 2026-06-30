package com.Veterinaria.Mejia.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.IngresoStock;
import com.Veterinaria.Mejia.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepo;

    // ==========================================
    // 1. HISTORIAL DE INGRESOS (Auditoría)
    // ==========================================
    @GetMapping
    public String listarHistorial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        // Configuramos la paginación con 10 registros por página, ordenados de más recientes a más antiguos
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaIngreso").descending());
        Page<IngresoStock> ingresosPage = ingresoStockService.listarHistorialPaginado(pageable);
        
        model.addAttribute("ingresosPage", ingresosPage);
        return "almacen/historial-ingresos";
    }

    // ==========================================
    // 2. FORMULARIO DE NUEVO INGRESO DE STOCK
    // ==========================================
    @GetMapping("/nuevo")
    public String nuevoIngreso(Model model) {
        model.addAttribute("ingresoStock", new IngresoStock());
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
                                      Authentication authentication,
                                      RedirectAttributes redirectAttrs) {
        try { // Add null check for user
            com.Veterinaria.Mejia.models.Usuario cajero = usuarioRepo.findByNombreUsuario(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado."));

            // El servicio valida (si la compra es en efectivo) que el saldo de
            // caja alcance ANTES de tocar stock, y descuenta automáticamente
            // sumar el stock físico a cada producto.
            ingresoStockService.registrarIngresoMercaderia(ingresoStock, cajero);

            redirectAttrs.addFlashAttribute("successMsg", "¡Stock actualizado y lote registrado con éxito!");
            return "redirect:/almacen/ingresos";

        } catch (RuntimeException e) {
            // Aquí también cae el mensaje de "Saldo de caja insuficiente..."
            // que lanza CajaService cuando la compra en efectivo no alcanza.
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/almacen/ingresos/nuevo";
        }
    }
}