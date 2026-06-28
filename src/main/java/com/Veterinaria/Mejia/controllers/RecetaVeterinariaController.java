package com.Veterinaria.Mejia.controllers;

import com.Veterinaria.Mejia.models.*;
import com.Veterinaria.Mejia.repository.*;
import com.Veterinaria.Mejia.services.RecetaVeterinariaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/recetas")
public class RecetaVeterinariaController {

    @Autowired private RecetaVeterinariaService recetaService;
    @Autowired private PacienteRepository pacienteRepo;
    @Autowired private UsuarioRepository usuarioRepo;
    @Autowired private ProductoRepository productoRepo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("recetas", recetaService.listarTodas());
        return "recetas/lista-recetas";
    }

    @GetMapping("/nueva")
    public String formNueva(Model model) {
        model.addAttribute("pacientes", pacienteRepo.findByEstadoTrue());
        model.addAttribute("veterinarios", usuarioRepo.findAll());
        model.addAttribute("productos", productoRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getEstado())).toList());
        return "recetas/form-receta";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam Integer pacienteId,
            @RequestParam Integer veterinarioId,
            @RequestParam(required = false) String cmpVeterinario,
            @RequestParam String diagnostico,
            @RequestParam(required = false) String indicaciones,
            @RequestParam(value = "productoId",   required = false) List<Integer>    productoIds,
            @RequestParam(value = "medicamento",  required = false) List<String>     medicamentos,
            @RequestParam(value = "cantidad",     required = false) List<BigDecimal> cantidades,
            @RequestParam(value = "unidadDosis",  required = false) List<String>     unidades,
            @RequestParam(value = "frecuencia",   required = false) List<String>     frecuencias,
            @RequestParam(value = "duracionDias", required = false) List<Integer>    duraciones,
            @RequestParam(value = "via",          required = false) List<String>     vias,
            @RequestParam(value = "indicLinea",   required = false) List<String>     indicLineas,
            Authentication auth, RedirectAttributes ra) {
        try {
            List<LineaReceta> lineas = new ArrayList<>();
            int total = productoIds != null ? productoIds.size() :
                        (medicamentos != null ? medicamentos.size() : 0);
            for (int i = 0; i < total; i++) {
                LineaReceta l = new LineaReceta();
                if (productoIds != null && i < productoIds.size() && productoIds.get(i) != null) {
                    productoRepo.findById(productoIds.get(i)).ifPresent(l::setProducto);
                }
                if (medicamentos != null && i < medicamentos.size()) l.setMedicamento(medicamentos.get(i));
                l.setCantidad(cantidades != null && i < cantidades.size() ? cantidades.get(i) : BigDecimal.ONE);
                if (unidades   != null && i < unidades.size())   l.setUnidadDosis(unidades.get(i));
                if (frecuencias!= null && i < frecuencias.size())l.setFrecuencia(frecuencias.get(i));
                if (duraciones != null && i < duraciones.size()) l.setDuracionDias(duraciones.get(i));
                if (vias       != null && i < vias.size())       l.setViaAdministracion(vias.get(i));
                if (indicLineas!= null && i < indicLineas.size())l.setIndicaciones(indicLineas.get(i));
                lineas.add(l);
            }
            RecetaVeterinaria r = recetaService.emitir(pacienteId, veterinarioId,
                    cmpVeterinario, diagnostico, indicaciones, lineas);
            ra.addFlashAttribute("successMsg", "✅ Receta " + r.getNumeroReceta() + " emitida correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
            return "redirect:/recetas/nueva";
        }
        return "redirect:/recetas";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Integer id, Model model) {
        RecetaVeterinaria r = recetaService.obtener(id);
        model.addAttribute("receta", r);
        model.addAttribute("lineas", r.getLineas());
        return "recetas/ver-receta";
    }

    @GetMapping("/anular/{id}")
    public String anular(@PathVariable Integer id, RedirectAttributes ra) {
        try { recetaService.anular(id); ra.addFlashAttribute("successMsg", "Receta anulada."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/recetas";
    }
}
