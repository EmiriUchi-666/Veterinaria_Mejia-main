package com.Veterinaria.Mejia.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.dto.ItemCarritoDTO;
import com.Veterinaria.Mejia.dto.VentaRequestDTO;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.services.CategoriaService;
import com.Veterinaria.Mejia.services.ClienteService;
import com.Veterinaria.Mejia.services.EspecieService;
import com.Veterinaria.Mejia.services.ProductoService;
import com.Veterinaria.Mejia.services.ServicioService;
import com.Veterinaria.Mejia.services.VentaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final ProductoService productoService;
    private final ServicioService servicioService;
    private final CategoriaService categoriaService;
    private final EspecieService especieService;
    private final ClienteService clienteService; 

    // ==========================================
    // 1. HISTORIAL DE VENTAS
    // ==========================================
    @GetMapping
    public String historialVentas(Model model) {
        model.addAttribute("ventas", ventaService.listarUltimas10Ventas()); 
        return "ventas/historial-ventas";
    }

    // ==========================================
    // 2. PANEL DE CAJA (NUEVA VENTA)
    // ==========================================
    @GetMapping("/nuevo")
    public String nuevaVenta(Model model) {
        Venta nuevaVenta = new Venta();
        
        // Hardcodeo temporal del ID de usuario encargado de la caja
        Usuario cajero = new Usuario();
        cajero.setId(1);
        nuevaVenta.setUsuario(cajero); 
        
        // Inicializa la lista vacía para el carrito
        nuevaVenta.setDetallesVentas(new ArrayList<>());
        
        // 🔥 CORRECCIÓN CRÍTICA: Previene el error de Thymeleaf al formatear nulos
        nuevaVenta.setTotalVenta(java.math.BigDecimal.ZERO);

        model.addAttribute("venta", nuevaVenta);
        
        // Carga de elementos comunes de la interfaz
        cargarElementosInterfaz(model);
        
        return "ventas/panel-caja";
    }

    // ==========================================
    // 3. PROCESAMIENTO DE TRANSACCIÓN
    // ==========================================
    @PostMapping("/procesar")
    public String procesarVenta(@ModelAttribute("venta") Venta venta, 
                                @RequestParam(value = "dniIngresado", required = false) String dniIngresado,
                                @RequestParam(value = "nombreIngresado", required = false) String nombreIngresado,
                                @RequestParam(value = "itemTipo", required = false) List<String> itemTipos,
                                @RequestParam(value = "itemId", required = false) List<Integer> itemIds,
                                @RequestParam(value = "itemCantidad", required = false) List<String> itemCantidades,
                                @RequestParam(value = "itemPrecio", required = false) List<String> itemPrecios,
                                @RequestParam(value = "itemSubtotal", required = false) List<String> itemSubtotales,
                                Model model) {

        try {
            // Validación de seguridad: Evitar procesar si la lista viene vacía
            if (itemTipos == null || itemTipos.isEmpty()) {
                throw new RuntimeException("El carrito no puede estar vacío.");
            }

            // 1. Construimos el DTO
            VentaRequestDTO request = new VentaRequestDTO();
            request.setClienteDni(dniIngresado);
            request.setClienteNombre(nombreIngresado);
            request.setTipoPago(venta.getTipoPago());
            
            // Aseguramos que el Total no falle por problemas de comas/puntos
            String totalStr = venta.getTotalVenta() != null ? venta.getTotalVenta().toString() : "0";
            request.setTotal(new java.math.BigDecimal(totalStr.replace(",", ".")));
            
            // 2. Extraemos las listas de arrays y las convertimos a números seguros
            List<ItemCarritoDTO> items = new ArrayList<>();
            for (int i = 0; i < itemTipos.size(); i++) {
                ItemCarritoDTO item = new ItemCarritoDTO();
                item.setTipo(itemTipos.get(i));
                item.setIdItem(itemIds.get(i));
                
                // El replace(",", ".") es el salvavidas contra los errores de idioma/decimales
                item.setCantidad(new java.math.BigDecimal(itemCantidades.get(i).replace(",", ".")));
                item.setPrecio(new java.math.BigDecimal(itemPrecios.get(i).replace(",", ".")));
                item.setSubtotal(new java.math.BigDecimal(itemSubtotales.get(i).replace(",", ".")));
                
                items.add(item);
            }
            request.setItems(items);

            // 3. Llamamos al servicio con el argumento DTO correcto
            Venta ventaProcesada = ventaService.procesarVentaTransaccional(request);
            
            return "redirect:/ventas/imprimir/" + ventaProcesada.getId();
            
        } catch (RuntimeException e) {
            model.addAttribute("errorStock", e.getMessage());
            cargarElementosInterfaz(model);
            venta.setTotalVenta(java.math.BigDecimal.ZERO);
            return "ventas/panel-caja";
        }
    }

    // ==========================================
    // 4. GENERACIÓN DE COMPROBANTE
    // ==========================================
    @GetMapping("/imprimir/{id}")
    public String mostrarTicketImpresion(@PathVariable("id") Integer id, Model model) {
        Venta ventaReal = ventaService.buscarPorId(id);
        model.addAttribute("venta", ventaReal);
        return "ventas/comprobante-ticket";
    }

    // ==========================================
    // 5. ANULAR VENTA
    // ==========================================
    @GetMapping("/anular/{id}")
    public String anularVenta(@PathVariable("id") Integer id, RedirectAttributes redirectAttrs) {
        try {
            ventaService.anularVenta(id);
            redirectAttrs.addFlashAttribute("successMsg", "La venta ha sido anulada y el stock fue devuelto correctamente al inventario.");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/ventas";
    }

    /**
     * Alimenta el modelo con los catálogos requeridos por la vista 'panel-caja'.
     */
    private void cargarElementosInterfaz(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("servicios", servicioService.listarActivosPOS());
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("especies", especieService.listarTodas());
        model.addAttribute("clientes", clienteService.findAll()); 
    }
}