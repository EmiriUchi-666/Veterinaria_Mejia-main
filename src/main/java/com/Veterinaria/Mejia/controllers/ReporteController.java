package com.Veterinaria.Mejia.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Veterinaria.Mejia.services.ReporteService;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping
    public String mostrarReportesFinancieros(
            @RequestParam(name = "rango", defaultValue = "hoy") String rango, 
            Model model) {

        // 1. Estandarizamos el filtro (hoy, semana, mes)
        String rangoSeleccionado = rango.toLowerCase();

        // 2. Generamos todas las métricas, gráficos y tops desde el Service
        Map<String, Object> datosReporte = reporteService.generarReporteDashboard(rangoSeleccionado);

        // 3. Enviamos todos los datos (cantidadVentas, valorInvertido, etc.) al HTML
        model.addAllAttributes(datosReporte);

        // 4. Enviamos el rango actual para que el botón correspondiente se pinte de "Activo" en la vista
        model.addAttribute("rangoActual", rangoSeleccionado);

        return "reportes/reportes"; // Tu archivo HTML de reportes
    }
}