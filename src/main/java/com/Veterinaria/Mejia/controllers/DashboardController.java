package com.Veterinaria.Mejia.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ReporteService reporteService;
    private final CajaService cajaService;
    private final PacienteRepository pacienteRepo;
    private final DuenoRepository duenoRepo;
    private final CitaRepository citaRepo;
    private final ProductoRepository productoRepo;

    @GetMapping("/dashboard")
    public String mostrarInicio(Model model) {
        // ── Métricas del día ───────────────────────────────────────────────
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        Map<String, Object> metricas = reporteService.generarReporteDashboard("hoy", inicioHoy, LocalDateTime.now());
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
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", Locale.of("es", "PE"))));

        return "reportes/dashboard";
    }
}
