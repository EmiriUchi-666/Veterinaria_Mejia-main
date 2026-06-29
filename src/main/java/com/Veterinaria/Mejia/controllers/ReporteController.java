package com.Veterinaria.Mejia.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Veterinaria.Mejia.services.ReporteService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public String mostrarReportesFinancieros(
            @RequestParam(name = "rango", defaultValue = "hoy") String rango,
            @RequestParam(name = "fechaInicio", required = false) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) LocalDate fechaFin,
            Model model) {

        // FASE 5: Centralizar cálculo de fechas
        LocalDateTime inicio, fin;
        switch (rango.toLowerCase()) {
            case "semana":
                inicio = LocalDate.now().minusDays(6).atStartOfDay();
                fin = LocalDateTime.now();
                break;
            case "mes":
                inicio = LocalDate.now().minusMonths(1).atStartOfDay();
                fin = LocalDateTime.now();
                break;
            case "personalizado":
                inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : LocalDate.now().atStartOfDay();
                fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : LocalDateTime.now();
                break;
            case "hoy":
            default:
                inicio = LocalDate.now().atStartOfDay();
                fin = LocalDateTime.now();
                break;
        }

        Map<String, Object> datosReporte = reporteService.generarReporteDashboard(rango, inicio, fin);

        model.addAllAttributes(datosReporte);
        model.addAttribute("rangoActual", rango);
        model.addAttribute("fechaInicio", inicio.toLocalDate());
        model.addAttribute("fechaFin", fin.toLocalDate());

        return "reportes/reportes"; // Tu archivo HTML de reportes
    }
}