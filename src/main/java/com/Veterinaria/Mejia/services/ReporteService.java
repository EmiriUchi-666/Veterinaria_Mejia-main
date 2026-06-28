package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.repository.DetalleVentaRepository;
import com.Veterinaria.Mejia.repository.MermaRepository;
import com.Veterinaria.Mejia.repository.VentaRepository;

@Service
public class ReporteService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private MermaRepository mermaRepository; // CONECTADO A LA TABLA MERMAS

    public Map<String, Object> generarReporteDashboard(String rango) {
        Map<String, Object> reporte = new HashMap<>();
        LocalDateTime fechaInicio = calcularFechaInicio(rango);

        List<Venta> ventasDelPeriodo = ventaRepository.findByFechaEmisionAfterAndEstado(fechaInicio, true);

        BigDecimal ingresosBrutos = calcularIngresosBrutos(ventasDelPeriodo);
        BigDecimal inversionVentas = calcularInversion(fechaInicio);
        
        // CÁLCULO DE PÉRDIDAS DESDE LA NUEVA TABLA
        BigDecimal perdidas = mermaRepository.calcularTotalPerdidasDesdeJPQL(fechaInicio);
        if (perdidas == null) perdidas = BigDecimal.ZERO;
        
        BigDecimal gananciaNeta = ingresosBrutos.subtract(inversionVentas).subtract(perdidas);

        reporte.put("cantidadVentas", ventasDelPeriodo.size());
        reporte.put("ingresosBrutos", ingresosBrutos);
        reporte.put("inversionVentas", inversionVentas);
        reporte.put("perdidas", perdidas);
        reporte.put("gananciaNeta", gananciaNeta);

        reporte.put("topProductos", detalleVentaRepository.findTop10ProductosVendidos(fechaInicio, null));
        
        Map<String, BigDecimal> datosAgrupados = agruparVentasParaGrafico(ventasDelPeriodo, rango);
        reporte.put("graficoLabels", new ArrayList<>(datosAgrupados.keySet()));
        reporte.put("graficoDatosVentas", new ArrayList<>(datosAgrupados.values()));
        reporte.put("graficoDatosGanancias", new ArrayList<>(datosAgrupados.values()));

        return reporte;
    }

    private LocalDateTime calcularFechaInicio(String rango) {
        LocalDateTime now = LocalDateTime.now();
        switch (rango) {
            case "semana": return now.minusDays(7).withHour(0).withMinute(0);
            case "mes": return now.minusMonths(1).withHour(0).withMinute(0);
            case "hoy": 
            default: return now.withHour(0).withMinute(0).withSecond(0);
        }
    }

    private BigDecimal calcularIngresosBrutos(List<Venta> ventas) {
        return ventas.stream()
                .map(Venta::getTotalVenta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularInversion(LocalDateTime fechaInicio) {
        BigDecimal inversion = detalleVentaRepository.calcularInversionDeVentasJPQL(fechaInicio);
        return inversion != null ? inversion : BigDecimal.ZERO;
    }

    private Map<String, BigDecimal> agruparVentasParaGrafico(List<Venta> ventas, String rango) {
        Map<String, BigDecimal> ventasAgrupadas = new TreeMap<>();
        DateTimeFormatter formatoEjeX = rango.equals("hoy") ? 
                                        DateTimeFormatter.ofPattern("HH:00") : 
                                        DateTimeFormatter.ofPattern("dd/MM");

        for (Venta v : ventas) {
            String etiqueta = v.getFechaEmision().format(formatoEjeX);
            BigDecimal montoActual = ventasAgrupadas.getOrDefault(etiqueta, BigDecimal.ZERO);
            ventasAgrupadas.put(etiqueta, montoActual.add(v.getTotalVenta()));
        }
        return ventasAgrupadas;
    }
}