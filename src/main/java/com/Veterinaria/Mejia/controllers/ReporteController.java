package com.Veterinaria.Mejia.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Veterinaria.Mejia.dto.ProductoRentabilidadDTO;
import com.Veterinaria.Mejia.dto.ServicioRentabilidadDTO;
import com.Veterinaria.Mejia.services.ExcelExportService;
import com.Veterinaria.Mejia.services.PdfExportService;
import com.Veterinaria.Mejia.services.ReporteService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

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
        List<ProductoRentabilidadDTO> rentabilidadProductos = reporteService.generarReporteRentabilidad(inicio, fin);
        List<ServicioRentabilidadDTO> rentabilidadServicios = reporteService.generarReporteRentabilidadServicios(inicio, fin);

        model.addAllAttributes(datosReporte);
        model.addAttribute("rentabilidadProductos", rentabilidadProductos);
        model.addAttribute("rentabilidadServicios", rentabilidadServicios);
        model.addAttribute("rangoActual", rango);
        model.addAttribute("fechaInicio", inicio.toLocalDate());
        model.addAttribute("fechaFin", fin.toLocalDate());

        return "reportes/reportes"; // Tu archivo HTML de reportes
    }

    @GetMapping("/exportar")
    public ResponseEntity<InputStreamResource> exportarAExcel(
            @RequestParam(name = "formato", defaultValue = "excel") String formato,
            @RequestParam(name = "fechaInicio") LocalDate fechaInicio,
            @RequestParam(name = "fechaFin") LocalDate fechaFin) throws IOException {

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        List<ProductoRentabilidadDTO> datos = reporteService.generarReporteRentabilidad(inicio, fin);
        ByteArrayInputStream in = null;
        String nombreArchivo = "reporte";
        String contentType = "";

        if ("excel".equals(formato)) {
            in = excelExportService.generarExcelRentabilidadProductos(datos);
            nombreArchivo = "Reporte_Rentabilidad.xlsx";
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if ("pdf".equals(formato)) {
            in = pdfExportService.generarPdfRentabilidadProductos(datos, inicio, fin);
            nombreArchivo = "Reporte_Rentabilidad.pdf";
            contentType = "application/pdf";
        } else {
            // Devolver un error o un archivo vacío si el formato no es soportado
            in = new ByteArrayInputStream(new byte[0]);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + nombreArchivo);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(in));
    }
}