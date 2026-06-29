package com.Veterinaria.Mejia.controllers;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.HistoriaClinica;
import com.Veterinaria.Mejia.models.Paciente;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.HistoriaClinicaRepository;
import com.Veterinaria.Mejia.repository.PacienteRepository;
import com.Veterinaria.Mejia.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/historias")
@RequiredArgsConstructor
public class HistoriaClinicaController {

    private final HistoriaClinicaRepository historiaRepo;
    private final PacienteRepository pacienteRepo;
    private final UsuarioRepository usuarioRepo;

    @GetMapping("/paciente/{pacienteId}")
    public String listarHistoriasPorPaciente(@PathVariable Integer pacienteId, Model model) {
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado:" + pacienteId));
        model.addAttribute("paciente", paciente);
        model.addAttribute("historias", historiaRepo.findByPacienteIdOrderByFechaAtencionDesc(pacienteId));
        return "historias/lista-historias";
    }

    @GetMapping("/nueva")
    public String formNuevaHistoria(@RequestParam("pacienteId") Integer pacienteId, Model model) {
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado:" + pacienteId));
        HistoriaClinica historia = new HistoriaClinica();
        historia.setPaciente(paciente);
        model.addAttribute("historia", historia);
        return "historias/form-historia";
    }

    @PostMapping("/guardar")
    public String guardarHistoria(@ModelAttribute HistoriaClinica historia, RedirectAttributes ra) {
        try {
            // Asignar paciente
            Paciente paciente = pacienteRepo.findById(historia.getPaciente().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Paciente no válido"));
            historia.setPaciente(paciente);

            // Asignar veterinario logueado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Usuario veterinario = usuarioRepo.findByNombreUsuario(username)
                    .orElseThrow(() -> new IllegalStateException("El usuario veterinario no fue encontrado en el sistema."));
            historia.setVeterinario(veterinario);

            historia.setFechaAtencion(LocalDateTime.now());

            historiaRepo.save(historia);
            ra.addFlashAttribute("successMsg", "Historia clínica registrada correctamente.");
            return "redirect:/pacientes/" + paciente.getId() + "/expediente";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al guardar la historia: " + e.getMessage());
            return "redirect:/historias/nueva?pacienteId=" + (historia.getPaciente() != null ? historia.getPaciente().getId() : "");
        }
    }

    @GetMapping("/{id}")
    public String verDetalleHistoria(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        return historiaRepo.findById(id)
                .map(historia -> {
                    model.addAttribute("historia", historia);
                    return "historias/ver-historia";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("errorMsg", "Historia clínica no encontrada.");
                    return "redirect:/pacientes";
                });
    }
}