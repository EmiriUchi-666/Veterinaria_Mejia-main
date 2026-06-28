package com.Veterinaria.Mejia.controllers;

import com.Veterinaria.Mejia.models.Cita;
import com.Veterinaria.Mejia.models.Paciente;
import com.Veterinaria.Mejia.models.Servicio;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaRepository citaRepo;

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private ServicioRepository servicioRepo;

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
        model.addAttribute("pacientes", pacienteRepo.findByEstadoTrue());
        model.addAttribute("veterinarios", usuarioRepo.findAll());
        model.addAttribute("servicios", servicioRepo.findAll());
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