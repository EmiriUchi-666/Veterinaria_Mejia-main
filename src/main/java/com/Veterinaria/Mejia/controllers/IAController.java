package com.Veterinaria.Mejia.controllers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Veterinaria.Mejia.dto.DiagnosticoDTO;
import com.Veterinaria.Mejia.dto.ResultadoDiagnosticoDTO;
import com.Veterinaria.Mejia.models.HistorialVacuna;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.repository.HistorialVacunaRepository;
import com.Veterinaria.Mejia.services.DiagnosticoService;
import com.Veterinaria.Mejia.services.IAPredictivaService;
import com.Veterinaria.Mejia.services.IAService;
import com.Veterinaria.Mejia.services.ProductoService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador del módulo de Inteligencia Artificial.
 * Diagnóstico heurístico, dashboard IA y IA predictiva de riesgos.
 */
@Controller
@RequestMapping("/ia")
@RequiredArgsConstructor
public class IAController {

    private final IAService iaService;
    private final DiagnosticoService diagnosticoService;
    private final IAPredictivaService iaPredictivaService;
    private final ProductoService productoService;
    private final HistorialVacunaRepository vacunaRepo;

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("diagnostico", new DiagnosticoDTO());
        return "ia/diagnostico";
    }

    @PostMapping("/analizar")
    public String analizar(@ModelAttribute DiagnosticoDTO dto, Model model) {
        ResultadoDiagnosticoDTO resultado = iaService.analizar(dto);
        model.addAttribute("diagnostico", dto);
        model.addAttribute("resultado", resultado);
        return "ia/diagnostico";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate hoy = LocalDate.now();
        long vacunasPendientes = vacunaRepo.contarVacunasPendientes(hoy, hoy.plusDays(30));
        List<HistorialVacuna> listaVacunas = vacunaRepo.findVacunasPendientes(hoy, hoy.plusDays(30));
        List<Producto> stockBajoLista = productoService.obtenerStockBajo();

        model.addAttribute("totalDiagnosticos",       diagnosticoService.contarDiagnosticos());
        model.addAttribute("casosCriticos",           iaPredictivaService.contarCasosAltoRiesgo());
        model.addAttribute("vacunasPendientes",       vacunasPendientes);
        model.addAttribute("productosStockBajo",      productoService.contarStockBajo());
        model.addAttribute("vacunasPendientesLista",  listaVacunas);
        model.addAttribute("productosStockBajoLista", stockBajoLista);
        model.addAttribute("diagnosticosLabels",      diagnosticoService.obtenerLabels());
        model.addAttribute("diagnosticosDatos",       diagnosticoService.obtenerDatos());
        model.addAttribute("prioridadLabels",         Arrays.asList("Crítico", "Urgente", "Normal", "Leve"));
        model.addAttribute("prioridadDatos",          Arrays.asList(
            iaPredictivaService.contarCasosAltoRiesgo(), 5L, 12L, 8L));
        return "ia/dashboard";
    }

    @GetMapping("/predictiva/{pacienteId}")
    public String iaPredictivaResultado(@PathVariable Integer pacienteId, Model model) {
        try {
            var riesgo = iaPredictivaService.evaluarRiesgoPaciente(pacienteId);
            model.addAttribute("riesgo", riesgo);
            model.addAttribute("paciente", riesgo.getPaciente());
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Error al evaluar: " + e.getMessage());
        }
        return "ia/predictiva";
    }
}
