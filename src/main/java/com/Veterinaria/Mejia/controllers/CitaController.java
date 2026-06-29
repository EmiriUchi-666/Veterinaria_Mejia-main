package com.Veterinaria.Mejia.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Cita;
import com.Veterinaria.Mejia.models.Paciente;
import com.Veterinaria.Mejia.models.Servicio;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.CitaRepository;
import com.Veterinaria.Mejia.repository.PacienteRepository;
import com.Veterinaria.Mejia.repository.ServicioRepository;
import com.Veterinaria.Mejia.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaRepository citaRepo;
    private final PacienteRepository pacienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final ServicioRepository servicioRepo;

    @GetMapping
    public String listarCitas(Model model) {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().plusDays(7).atTime(23, 59);
        model.addAttribute("citas", citaRepo.findByFechaHoraBetweenOrderByFechaHoraAsc(inicio, fin));
        return "citas/lista-citas";
    }

    @GetMapping("/nuevo")
    public String formNuevaCita(Model model) {
        model.addAttribute("cita", new Cita());
        // Hacemos explícitos los tipos para usar los imports y mejorar la legibilidad
        List<Paciente> pacientes = pacienteRepo.findByEstadoTrue();
        List<Usuario> veterinarios = usuarioRepo.findAll();
        List<Servicio> servicios = servicioRepo.findAll();
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("veterinarios", veterinarios);
        model.addAttribute("servicios", servicios);
        return "citas/form-cita";
    }

    @PostMapping("/guardar")
    public String guardarCita(@ModelAttribute Cita cita, RedirectAttributes ra) {
        if (cita.getFechaHora() == null) {
            cita.setFechaHora(LocalDateTime.now());
        }
        if (cita.getEstado() == null) {
            cita.setEstado(Cita.EstadoCita.Pendiente);
        }
        if (cita.getEsVisitaExterna() == null) {
            cita.setEsVisitaExterna(false);
        }

        citaRepo.save(cita);
        ra.addFlashAttribute("successMsg", "Cita registrada correctamente.");
        return "redirect:/citas";
    }

    @GetMapping("/cambiar-estado/{id}/{estado}")
    @SuppressWarnings("null")
    public String cambiarEstado(@PathVariable Integer id, @PathVariable String estado, RedirectAttributes ra) {
        Cita cita = citaRepo.findById(id).orElse(null);
        if (cita != null) {
            cita.setEstado(Cita.EstadoCita.valueOf(estado));
            citaRepo.save(cita);
            ra.addFlashAttribute("successMsg", "Estado de cita actualizado.");
        }
        return "redirect:/citas";
    }
}