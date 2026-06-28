package com.Veterinaria.Mejia.controllers;

import com.Veterinaria.Mejia.models.*;
import com.Veterinaria.Mejia.repository.*;
import com.Veterinaria.Mejia.services.TratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de Tratamientos Médicos.
 * Permite registrar tratamientos con trazabilidad de medicamentos
 * (descuenta stock automáticamente al guardar).
 */
@Controller
@RequestMapping("/tratamientos")
public class TratamientoController {

    @Autowired
    private TratamientoService tratamientoService;

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepo;

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private ProductoRepository productoRepo;

    /**
     * Lista los tratamientos de un paciente específico.
     */
    @GetMapping("/paciente/{pacienteId}")
    public String listarPorPaciente(@PathVariable Integer pacienteId, Model model) {
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        List<Tratamiento> tratamientos = tratamientoService.listarPorPaciente(pacienteId);
        model.addAttribute("paciente", paciente);
        model.addAttribute("tratamientos", tratamientos);
        return "tratamiento/lista-tratamientos";
    }

    /**
     * Muestra el formulario para registrar un nuevo tratamiento.
     * Requiere el ID de la historia clínica asociada.
     */
    @GetMapping("/nuevo/{historiaClinicaId}")
    @PreAuthorize("hasAnyAuthority('ROLE_Veterinario', 'ROLE_Administrador')")
    public String formNuevoTratamiento(@PathVariable Integer historiaClinicaId, Model model) {
        HistoriaClinica historia = historiaClinicaRepo.findById(historiaClinicaId)
                .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada"));

        model.addAttribute("historia", historia);
        model.addAttribute("productos", productoRepo.findAll()
                .stream()
                .filter(p -> p.getEstado() && p.getStockTotal().compareTo(BigDecimal.ZERO) > 0)
                .toList());
        return "tratamiento/form-tratamiento";
    }

    /**
     * Guarda el tratamiento, procesa los detalles de medicamentos y descuenta el stock.
     */
    @PostMapping("/guardar")
    @PreAuthorize("hasAnyAuthority('ROLE_Veterinario', 'ROLE_Administrador')")
    public String guardarTratamiento(
            @RequestParam Integer historiaClinicaId,
            @RequestParam String diagnostico,
            @RequestParam(required = false) String observaciones,
            @RequestParam(value = "productoId", required = false) List<Integer> productoIds,
            @RequestParam(value = "cantidad", required = false) List<BigDecimal> cantidades,
            @RequestParam(value = "lote", required = false) List<String> lotes,
            @RequestParam(value = "obsDetalle", required = false) List<String> obsDetalles,
            RedirectAttributes ra) {

        // Construir lista de detalles desde los arrays del formulario
        List<DetalleTratamiento> detalles = new ArrayList<>();
        if (productoIds != null) {
            for (int i = 0; i < productoIds.size(); i++) {
                if (productoIds.get(i) != null && cantidades.get(i) != null) {
                    DetalleTratamiento d = new DetalleTratamiento();
                    Producto p = new Producto();
                    p.setId(productoIds.get(i));
                    d.setProducto(p);
                    d.setCantidadUsada(cantidades.get(i));
                    if (lotes != null && i < lotes.size()) d.setLote(lotes.get(i));
                    if (obsDetalles != null && i < obsDetalles.size()) d.setObservaciones(obsDetalles.get(i));
                    detalles.add(d);
                }
            }
        }

        try {
            HistoriaClinica historia = historiaClinicaRepo.findById(historiaClinicaId)
                    .orElseThrow(() -> new RuntimeException("Historia no encontrada"));
            tratamientoService.registrarTratamiento(historiaClinicaId, diagnostico, observaciones, detalles);
            ra.addFlashAttribute("successMsg", "Tratamiento registrado con trazabilidad de medicamentos.");
            return "redirect:/tratamientos/paciente/" + historia.getPaciente().getId();

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/tratamientos/nuevo/" + historiaClinicaId;
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al registrar: " + e.getMessage());
            return "redirect:/tratamientos/nuevo/" + historiaClinicaId;
        }
    }

    /**
     * Cambia el estado de un tratamiento.
     */
    @GetMapping("/estado/{id}/{estado}")
    @PreAuthorize("hasAnyAuthority('ROLE_Veterinario', 'ROLE_Administrador')")
    public String cambiarEstado(@PathVariable Integer id,
                                @PathVariable String estado,
                                @RequestParam(required = false) Integer pacienteId,
                                RedirectAttributes ra) {
        try {
            tratamientoService.cambiarEstado(id, Tratamiento.EstadoTratamiento.valueOf(estado));
            ra.addFlashAttribute("successMsg", "Estado del tratamiento actualizado a: " + estado);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage());
        }
        return "redirect:/tratamientos/paciente/" + (pacienteId != null ? pacienteId : "");
    }

    /**
     * Ver detalles de un tratamiento específico.
     */
    @GetMapping("/detalle/{id}")
    public String verDetalle(@PathVariable Integer id, Model model) {
        Tratamiento t = tratamientoService.obtenerPorId(id);
        model.addAttribute("tratamiento", t);
        model.addAttribute("detalles", tratamientoService.obtenerDetalles(id));
        return "tratamiento/detalle-tratamiento";
    }
}
