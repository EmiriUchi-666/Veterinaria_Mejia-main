package com.Veterinaria.Mejia.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.DetalleTratamiento;
import com.Veterinaria.Mejia.models.HistoriaClinica;
import com.Veterinaria.Mejia.models.Paciente;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.models.Tratamiento;
import com.Veterinaria.Mejia.repository.HistoriaClinicaRepository;
import com.Veterinaria.Mejia.repository.PacienteRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;
import com.Veterinaria.Mejia.services.PdfService;
import com.Veterinaria.Mejia.services.TratamientoService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador de Tratamientos Médicos.
 * Permite registrar tratamientos con trazabilidad de medicamentos
 * (descuenta stock automáticamente al guardar).
 */
@Controller
@RequestMapping("/tratamientos")
@RequiredArgsConstructor
public class TratamientoController {

    private final TratamientoService tratamientoService;
    private final HistoriaClinicaRepository historiaClinicaRepo;
    private final PacienteRepository pacienteRepo;
    private final ProductoRepository productoRepo;
    private final PdfService pdfService;

    @GetMapping({"", "/"})
    public String listarTodosTratamientos(Model model) {
        // Listar todos los tratamientos ordenados por fecha descendente
        List<Tratamiento> todos = tratamientoService.listarTodos(); // crear este método en el service si no existe
        model.addAttribute("tratamientos", todos);
        return "tratamiento/lista-tratamientos";
    }
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
    @GetMapping("/nuevo")
    @PreAuthorize("hasAnyAuthority('ROLE_Veterinario', 'ROLE_Administrador')")
    public String formNuevoTratamiento(@RequestParam(name = "historiaClinicaId", required = false) Integer historiaClinicaId, Model model) {
        
        if (historiaClinicaId != null) {
            HistoriaClinica historia = historiaClinicaRepo.findById(historiaClinicaId)
                    .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada"));
            model.addAttribute("historia", historia);
        }
        
        model.addAttribute("pacientes", pacienteRepo.findByEstadoTrue());

        model.addAttribute("productos", productoRepo.findByEstadoTrueAndUsoClinicoTrue()
                .stream()
                .toList());
        return "tratamiento/form-tratamiento";
    }

    /**
     * Guarda el tratamiento, procesa los detalles de medicamentos y descuenta el stock.
     */
    @PostMapping("/guardar")
    @PreAuthorize("hasAnyAuthority('ROLE_Veterinario', 'ROLE_Administrador')")
    public String guardarTratamiento(
            @RequestParam(required = false) Integer historiaClinicaId,
            @RequestParam(required = false) Integer pacienteId,
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
            Integer redirectId;
            if (historiaClinicaId != null) {
                HistoriaClinica historia = historiaClinicaRepo.findById(historiaClinicaId)
                        .orElseThrow(() -> new RuntimeException("Historia no encontrada"));
                tratamientoService.registrarTratamiento(historiaClinicaId, diagnostico, observaciones, detalles);
                redirectId = historia.getPaciente().getId();
            } else {
                tratamientoService.registrarTratamientoGeneral(pacienteId, diagnostico, observaciones, detalles);
                redirectId = pacienteId;
            }
            ra.addFlashAttribute("successMsg", "Tratamiento registrado con trazabilidad de medicamentos.");
            return "redirect:/pacientes/" + redirectId + "/expediente";

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/tratamientos/nuevo" + (historiaClinicaId != null ? "?historiaClinicaId=" + historiaClinicaId : "");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al registrar: " + e.getMessage());
            return "redirect:/tratamientos/nuevo" + (historiaClinicaId != null ? "?historiaClinicaId=" + historiaClinicaId : "");
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

    @GetMapping("/detalle/{id}/pdf")
    public ResponseEntity<byte[]> generarPdfTratamiento(@PathVariable Integer id) {
        Tratamiento tratamiento = tratamientoService.obtenerPorId(id);
        List<DetalleTratamiento> detalles = tratamientoService.obtenerDetalles(id);

        Map<String, Object> variables = new HashMap<>();
        variables.put("tratamiento", tratamiento);
        variables.put("detalles", detalles);

        byte[] pdfBytes = pdfService.generarPdfDesdeHtml("pdf/tratamiento-pdf", variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "tratamiento-" + tratamiento.getId() + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
