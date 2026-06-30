package com.Veterinaria.Mejia.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.services.CategoriaService;
import com.Veterinaria.Mejia.services.ClienteService;
import com.Veterinaria.Mejia.services.EspecieService;
import com.Veterinaria.Mejia.services.ProductoService;
import com.Veterinaria.Mejia.services.ServicioService;
import com.Veterinaria.Mejia.services.VentaFacade;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ventas")
public class VentaController {

    // Se utiliza inyección por constructor (recomendado) en lugar de @Autowired
    private final VentaFacade ventaFacade;
    private final ProductoService productoService;
    private final ServicioService servicioService;
    private final CategoriaService categoriaService;
    private final EspecieService especieService;
    private final ClienteService clienteService;

    // ==========================================
    // 1. HISTORIAL DE VENTAS
    // ==========================================
    @GetMapping({"", "/", "/historial"})
    public String historialVentas(Model model) { 
        model.addAttribute("ventas", ventaFacade.listarUltimas10VentasConDetalles());
        return "ventas/historial-ventas";
    }

    // ==========================================
    // 2. PANEL DE CAJA (NUEVA VENTA)
    // ==========================================
    @GetMapping("/nuevo")
    public String nuevaVenta(Model model) {
        Venta nuevaVenta = new Venta();
        
        // Inicialización segura para la vista
        nuevaVenta.setDetallesVentas(new ArrayList<>());
        nuevaVenta.setTotalVenta(BigDecimal.ZERO);

        model.addAttribute("venta", nuevaVenta);
        cargarElementosInterfaz(model);
        
        return "ventas/panel-caja";
    }

    // ==========================================
    // 3. PROCESAMIENTO DE TRANSACCIÓN
    // ==========================================
    @PostMapping("/procesar")
    public String procesarVenta(
            @ModelAttribute("venta") Venta venta,
            @RequestParam(value = "dniIngresado", required = false) String dniIngresado,
            @RequestParam(value = "nombreIngresado", required = false) String nombreIngresado,
            @RequestParam(value = "itemTipo", required = false) List<String> itemTipos,
            @RequestParam(value = "itemId", required = false) List<Integer> itemIds,
            @RequestParam(value = "itemCantidad", required = false) List<String> itemCantidades,
            @RequestParam(value = "itemPrecio", required = false) List<String> itemPrecios,
            @RequestParam(value = "itemSubtotal", required = false) List<String> itemSubtotales,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            // Validación para evitar ventas con monto cero o negativo
            if (venta.getTotalVenta() == null || venta.getTotalVenta().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("No se puede procesar una venta con un monto total de cero o negativo.");
            }

            // El Facade ahora se encarga de construir el DTO y procesar la venta.
            Venta ventaProcesada = ventaFacade.procesarVentaDesdeFormulario(
                venta, dniIngresado, nombreIngresado, 
                itemTipos, itemIds, itemCantidades, itemPrecios, itemSubtotales, 
                authentication
            );
            
            redirectAttributes.addFlashAttribute("successMsg", "Venta #" + ventaProcesada.getId() + " registrada exitosamente.");
            return "redirect:/ventas/imprimir/" + ventaProcesada.getId();
            
        } catch (IllegalArgumentException e) {
            // Captura errores de validación de negocio (ej. monto cero, carrito vacío)
            model.addAttribute("errorMsg", e.getMessage());
            cargarElementosInterfaz(model);
            venta.setTotalVenta(BigDecimal.ZERO);
            return "ventas/panel-caja";
        } 
        catch (RuntimeException e) {
            // Manejo de errores regresando a la vista del panel con los catálogos cargados
            model.addAttribute("errorMsg", "Error al procesar la venta: " + e.getMessage());
            cargarElementosInterfaz(model);
            venta.setTotalVenta(BigDecimal.ZERO);
            return "ventas/panel-caja";
        }
    }

    // ==========================================
    // 4. GENERACIÓN DE COMPROBANTE
    // ==========================================
    @GetMapping("/imprimir/{id}")
    public String mostrarTicketImpresion(@PathVariable("id") Integer id, Model model) {
        Venta ventaReal = ventaFacade.buscarPorId(id);
        model.addAttribute("venta", ventaReal);
        return "ventas/comprobante-ticket";
    }

    // ==========================================
    // 5. ANULAR VENTA
    // ==========================================
    @GetMapping("/anular/{id}")
    public String anularVenta(@PathVariable("id") Integer id, RedirectAttributes redirectAttrs) {
        try {
            ventaFacade.anularVenta(id);
            redirectAttrs.addFlashAttribute("successMsg", "La venta ha sido anulada y el stock fue devuelto correctamente.");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMsg", "Error al anular: " + e.getMessage());
        }
        return "redirect:/ventas/historial";
    }

    // ==========================================
    // MÉTODOS PRIVADOS AUXILIARES (CLEAN CODE)
    // ==========================================
    
    /**
     * Alimenta el modelo con los catálogos requeridos por la vista 'panel-caja'.
     */
    private void cargarElementosInterfaz(Model model) {
        // FASE 11: Cargar solo productos de venta al público, no de uso clínico.
        model.addAttribute("productos", productoService.findByEstadoTrueAndUsoClinicoFalse());
        model.addAttribute("servicios", servicioService.listarActivosPOS());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("especies", especieService.listarTodas());
        model.addAttribute("clientes", clienteService.findAll());
    }
}