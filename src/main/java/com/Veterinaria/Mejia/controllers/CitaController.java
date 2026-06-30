package com.Veterinaria.Mejia.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
import com.Veterinaria.Mejia.repository.DuenoRepository;
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
    private final DuenoRepository duenoRepo;
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
        return prepararFormulario(new Cita(), model);
    }

    /**
     * Abre el formulario de cita ya existente con todos sus datos precargados,
     * para poder editarla (fecha, veterinario, servicio, tipo de visita, etc.).
     */
    @GetMapping("/editar/{id}")
    public String editarCita(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Optional<Cita> citaOpt = citaRepo.findByIdWithDetalles(id);
        if (citaOpt.isEmpty()) {
            ra.addFlashAttribute("errorMsg", "La cita que intentas editar no existe o ya fue eliminada.");
            return "redirect:/citas";
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.Cancelada) {
            ra.addFlashAttribute("errorMsg", "No se puede editar una cita que ya fue cancelada.");
            return "redirect:/citas";
        }
        if (cita.getEstado() == Cita.EstadoCita.Atendida) {
            ra.addFlashAttribute("errorMsg", "No se puede editar una cita que ya fue atendida.");
            return "redirect:/citas";
        }
        return prepararFormulario(cita, model);
    }

    private String prepararFormulario(Cita cita, Model model) {
        model.addAttribute("cita", cita);
        List<Paciente> pacientes = pacienteRepo.findByEstadoTrue();
        List<Usuario> veterinarios = usuarioRepo.findAll();
        List<Servicio> servicios = servicioRepo.findAll();
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("duenos", duenoRepo.findByEstadoTrueOrderByNombreAsc());
        model.addAttribute("veterinarios", veterinarios);
        model.addAttribute("servicios", servicios);
        return "citas/form-cita";
    }

    @PostMapping("/guardar")
    public String guardarCita(@ModelAttribute Cita cita, RedirectAttributes ra) {
        boolean esEdicion = cita.getId() != null;

        if (esEdicion) {
            // Se verifica que la cita exista y no se intente editar una ya
            // cancelada o atendida (por si se manipula el formulario a mano).
            Cita citaExistente = citaRepo.findById(cita.getId()).orElse(null);
            if (citaExistente == null) {
                ra.addFlashAttribute("errorMsg", "La cita que intentas actualizar ya no existe.");
                return "redirect:/citas";
            }
            if (citaExistente.getEstado() == Cita.EstadoCita.Cancelada
                    || citaExistente.getEstado() == Cita.EstadoCita.Atendida) {
                ra.addFlashAttribute("errorMsg", "No se puede editar una cita Cancelada o Atendida.");
                return "redirect:/citas";
            }
            // Conserva el estado actual de la cita (no se toca desde este
            // formulario; el estado se cambia con los botones dedicados).
            cita.setEstado(citaExistente.getEstado());
        }

        if (cita.getFechaHora() == null) {
            cita.setFechaHora(LocalDateTime.now());
        } else if (!esEdicion && cita.getFechaHora().isBefore(LocalDateTime.now())) {
            ra.addFlashAttribute("errorMsg", "No se puede programar una cita en una fecha/hora pasada.");
            return "redirect:/citas/nuevo";
        }
        if (cita.getEstado() == null) {
            cita.setEstado(Cita.EstadoCita.Pendiente);
        }
        if (cita.getEsVisitaExterna() == null) {
            cita.setEsVisitaExterna(false);
        }

        // FIX: si la cita es atención en el local (no domicilio), la
        // dirección se rellena automáticamente con "Local de la
        // Veterinaria" en vez de quedar vacía o con basura de un toggle
        // anterior. Esto se hace en el backend (no solo en JS) para que
        // quede consistente sin importar cómo llegue el formulario.
        if (!Boolean.TRUE.equals(cita.getEsVisitaExterna())) {
            cita.setDireccionVisita("Local de la Veterinaria");
            cita.setReferenciaUbicacion(null);
        }

        citaRepo.save(cita);
        ra.addFlashAttribute("successMsg", esEdicion ? "Cita actualizada correctamente." : "Cita registrada correctamente.");
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