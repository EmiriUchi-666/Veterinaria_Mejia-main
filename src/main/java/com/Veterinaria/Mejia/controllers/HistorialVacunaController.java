package com.Veterinaria.Mejia.controllers;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.HistorialVacuna;
import com.Veterinaria.Mejia.models.Paciente;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.repository.HistorialVacunaRepository;
import com.Veterinaria.Mejia.repository.PacienteRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/vacunas")
@RequiredArgsConstructor
public class HistorialVacunaController {

    private final HistorialVacunaRepository vacunaRepo;
    private final PacienteRepository pacienteRepo;
    private final ProductoRepository productoRepo;

    @GetMapping("/paciente/{pacienteId}")
    public String historialVacunas(@PathVariable Integer pacienteId, Model model) {
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado:" + pacienteId));
        model.addAttribute("paciente", paciente);
        model.addAttribute("vacunas", vacunaRepo.findByPacienteId(pacienteId));
        model.addAttribute("productosVacuna", productoRepo.findByCategoriaNombre("Vacunas"));
        model.addAttribute("nuevaVacuna", new HistorialVacuna());
        return "vacunas/historial-vacunas";
    }

    @PostMapping("/guardar")
    public String guardarVacuna(@ModelAttribute HistorialVacuna vacuna,
                                @RequestParam("pacienteId") Integer pacienteId,
                                @RequestParam(value = "productoId", required = false) Integer productoId,
                                RedirectAttributes ra) {
        try {
            Paciente paciente = pacienteRepo.findById(pacienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
            vacuna.setPaciente(paciente);

            if (productoId != null) {
                Producto producto = productoRepo.findById(productoId)
                        .orElseThrow(() -> new IllegalArgumentException("Producto (vacuna) no encontrado"));
                vacuna.setProductoVacuna(producto);
                vacuna.setNombreVacuna(producto.getNombre()); // Autocompletar nombre
            }

            vacuna.setFechaAplicacion(LocalDate.now());
            vacunaRepo.save(vacuna);
            ra.addFlashAttribute("successMsg", "Vacuna registrada correctamente en el historial.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al registrar la vacuna: " + e.getMessage());
            if (pacienteId != null) {
                return "redirect:/pacientes/" + pacienteId + "/expediente";
            }
            return "redirect:/pacientes"; // Fallback a la lista general si el ID es nulo
        }
        return "redirect:/pacientes/" + pacienteId + "/expediente";
    }
}