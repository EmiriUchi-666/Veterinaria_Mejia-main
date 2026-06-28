package com.Veterinaria.Mejia.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.Veterinaria.Mejia.models.AperturaCierreCaja;
import com.Veterinaria.Mejia.repository.CitaRepository;
import com.Veterinaria.Mejia.repository.DuenoRepository;
import com.Veterinaria.Mejia.repository.PacienteRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;
import com.Veterinaria.Mejia.services.CajaService;
import com.Veterinaria.Mejia.services.ReporteService;

@Controller
public class DashboardController {

    @Autowired private ReporteService reporteService;
    @Autowired private CajaService cajaService;
    @Autowired private PacienteRepository pacienteRepo;
    @Autowired private DuenoRepository duenoRepo;
    @Autowired private CitaRepository citaRepo;
    @Autowired private ProductoRepository productoRepo;

    @GetMapping("/dashboard")
    public String mostrarInicio(Model model) {
        // ── Métricas del día ───────────────────────────────────────────────
        Map<String, Object> metricas = reporteService.generarReporteDashboard("hoy");
        model.addAttribute("cantidadVentas", metricas.get("cantidadVentas"));
        model.addAttribute("gananciaNeta",   metricas.get("gananciaNeta"));
        model.addAttribute("ingresosBrutos", metricas.get("ingresosBrutos"));

        // ── Contadores globales ────────────────────────────────────────────
        model.addAttribute("totalPacientes", pacienteRepo.count());
        model.addAttribute("totalDuenos",    duenoRepo.countByEstadoTrue());
        model.addAttribute("totalCitas",     citaRepo.count());
        model.addAttribute("productosStockBajo",
            productoRepo.findAll().stream().filter(p ->
                Boolean.TRUE.equals(p.getEstado()) &&
                p.getStockTotal().compareTo(p.getStockMinimo()) < 0).count());

        // ── Estado de caja ─────────────────────────────────────────────────
        Optional<AperturaCierreCaja> caja = cajaService.getCajaAbierta();
        model.addAttribute("cajaAbierta", caja.orElse(null));
        model.addAttribute("hayCaja", caja.isPresent());
        // ✅ SOLUCIÓN 1: Reutilizar tu método seguro
        if (caja.isPresent()) {
            AperturaCierreCaja c = caja.get();
            BigDecimal saldo = c.getSaldoActual(); 
            model.addAttribute("saldoCaja", saldo);
        }

        // ── Fecha actual ───────────────────────────────────────────────────
        model.addAttribute("fechaHoy",
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy",
                new java.util.Locale("es", "PE"))));

        return "reportes/dashboard";
    }
}
