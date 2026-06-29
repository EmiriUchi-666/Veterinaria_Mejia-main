package com.Veterinaria.Mejia.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Cliente;
import com.Veterinaria.Mejia.models.CotizacionQuirurgica;
import com.Veterinaria.Mejia.models.DesgloseItem;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.models.Servicio;
import com.Veterinaria.Mejia.repository.ClienteRepository;
import com.Veterinaria.Mejia.repository.CotizacionQuirurgicaRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;
import com.Veterinaria.Mejia.repository.ServicioRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cotizaciones")
@RequiredArgsConstructor
public class CotizacionQuirurgicaController {

    private final CotizacionQuirurgicaRepository cotizacionRepo;
    private final ClienteRepository clienteRepo;
    private final ProductoRepository productoRepo;
    private final ServicioRepository servicioRepo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cotizaciones", cotizacionRepo.findAll());
        return "cotizaciones/lista-cotizaciones";
    }

    @GetMapping("/nueva")
    public String formNueva(Model model) {
        model.addAttribute("cotizacion", new CotizacionQuirurgica());
        model.addAttribute("clientes", clienteRepo.findAll());
        model.addAttribute("productos", productoRepo.findAll());
        model.addAttribute("servicios", servicioRepo.findAll());
        return "cotizaciones/form-cotizacion";
    }

    @PostMapping("/guardar")
    @SuppressWarnings("null")
    public String guardar(
            @RequestParam Integer clienteId,
            @RequestParam String procedimiento,
            @RequestParam BigDecimal total,
            @RequestParam(value = "itemTipo", required = false) List<String> itemTipos,
            @RequestParam(value = "itemId", required = false) List<Integer> itemIds,
            @RequestParam(value = "itemDescripcion", required = false) List<String> itemDescripciones,
            @RequestParam(value = "itemCantidad", required = false) List<BigDecimal> itemCantidades,
            @RequestParam(value = "itemCosto", required = false) List<BigDecimal> itemCostos,
            RedirectAttributes ra) {

        try {
            Cliente cliente = clienteRepo.findById(clienteId).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            
            CotizacionQuirurgica cotizacion = new CotizacionQuirurgica();
            cotizacion.setCliente(cliente);
            cotizacion.setProcedimiento(procedimiento);
            cotizacion.setTotal(total);
            cotizacion.setFecha(LocalDateTime.now());

            List<DesgloseItem> desglose = new ArrayList<>();
            if (itemTipos != null) {
                for (int i = 0; i < itemTipos.size(); i++) {
                    DesgloseItem item = new DesgloseItem();
                    if ("producto".equals(itemTipos.get(i))) {
                        Producto p = productoRepo.findById(itemIds.get(i)).orElse(null);
                        item.setProducto(p);
                    } else { // servicio
                        Servicio s = servicioRepo.findById(itemIds.get(i)).orElse(null);
                        item.setServicio(s);
                    }
                    item.setDescripcion(itemDescripciones.get(i));
                    item.setCantidad(itemCantidades.get(i));
                    item.setCostoUnitario(itemCostos.get(i));
                    desglose.add(item);
                }
            }
            cotizacion.setDesgloseItems(desglose);
            cotizacionRepo.save(cotizacion);
            ra.addFlashAttribute("successMsg", "Cotización guardada exitosamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al guardar la cotización: " + e.getMessage());
            return "redirect:/cotizaciones/nueva";
        }
        return "redirect:/cotizaciones";
    }
}