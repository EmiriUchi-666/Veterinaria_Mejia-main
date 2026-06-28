package com.Veterinaria.Mejia.controllers;

import com.Veterinaria.Mejia.models.MedicamentoControlado;
import com.Veterinaria.Mejia.repository.ProductoRepository;
import com.Veterinaria.Mejia.services.MedicamentoControladoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/medicamentos-controlados")
public class MedicamentoControladoController {

    @Autowired private MedicamentoControladoService service;
    @Autowired private ProductoRepository productoRepo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("medicamentos", service.listarTodos());
        model.addAttribute("categorias", MedicamentoControlado.CategoriaMedicamento.values());
        return "medicamentos/lista-controlados";
    }

    @GetMapping("/nuevo")
    public String formNuevo(Model model) {
        model.addAttribute("productos", productoRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getEstado())).toList());
        model.addAttribute("categorias", MedicamentoControlado.CategoriaMedicamento.values());
        return "medicamentos/form-controlado";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam Integer productoId,
            @RequestParam String categoria,
            @RequestParam(required = false) String principioActivo,
            @RequestParam(required = false) String registroSanitario,
            @RequestParam(required = false) String laboratorio,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes ra) {
        try {
            service.registrar(productoId,
                    MedicamentoControlado.CategoriaMedicamento.valueOf(categoria),
                    principioActivo, registroSanitario, laboratorio, observaciones);
            ra.addFlashAttribute("successMsg", "Medicamento registrado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
            return "redirect:/medicamentos-controlados/nuevo";
        }
        return "redirect:/medicamentos-controlados";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        try { service.eliminar(id); ra.addFlashAttribute("successMsg", "Registro eliminado."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/medicamentos-controlados";
    }
}
