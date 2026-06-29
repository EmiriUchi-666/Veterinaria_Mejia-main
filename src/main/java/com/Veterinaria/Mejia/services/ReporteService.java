package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.Veterinaria.Mejia.dto.ProductoRentabilidadDTO;
import com.Veterinaria.Mejia.dto.ServicioRentabilidadDTO;
import com.Veterinaria.Mejia.dto.VentasDiaDTO;
import com.Veterinaria.Mejia.repository.DetalleVentaRepository;
import com.Veterinaria.Mejia.repository.MermaRepository;
import com.Veterinaria.Mejia.repository.VentaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final MermaRepository mermaRepository; // CONECTADO A LA TABLA MERMAS

    public Map<String, Object> generarReporteDashboard(String rango, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Map<String, Object> reporte = new HashMap<>();

        // FASE 5: Usar queries de agregación en lugar de cargar toda la lista de ventas.
        long cantidadVentas = ventaRepository.contarVentasPorFechaJPQL(fechaInicio, fechaFin);
        BigDecimal ingresosBrutos = ventaRepository.sumarIngresosPorFechaJPQL(fechaInicio, fechaFin);
        BigDecimal inversionVentas = detalleVentaRepository.calcularInversionDeVentasJPQL(fechaInicio);
        if (inversionVentas == null) inversionVentas = BigDecimal.ZERO;

        // CÁLCULO DE PÉRDIDAS DESDE LA NUEVA TABLA
        BigDecimal perdidas = mermaRepository.calcularTotalPerdidasDesdeJPQL(fechaInicio);
        if (perdidas == null) perdidas = BigDecimal.ZERO;
        
        BigDecimal gananciaNeta = ingresosBrutos.subtract(inversionVentas).subtract(perdidas);

        reporte.put("cantidadVentas", cantidadVentas);
        reporte.put("ingresosBrutos", ingresosBrutos);
        reporte.put("inversionVentas", inversionVentas);
        reporte.put("perdidas", perdidas);
        reporte.put("gananciaNeta", gananciaNeta);

        // FASE 5: Usar query de agregación para el gráfico.
        List<VentasDiaDTO> datosGrafico = ventaRepository.obtenerGraficoVentasPorFechaJPQL(fechaInicio, fechaFin);
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        DateTimeFormatter formatoEjeX = rango.equals("hoy") ?
                DateTimeFormatter.ofPattern("HH:00") :
                DateTimeFormatter.ofPattern("dd/MM");

        datosGrafico.forEach(dto -> {
            // La query nativa devuelve un String, la JPQL un objeto Date/LocalDate
            if (dto.getFecha() instanceof String) {
                labels.add(dto.getFecha().toString());
            } else if (dto.getFecha() instanceof java.sql.Date) {
                labels.add(((java.sql.Date) dto.getFecha()).toLocalDate().format(formatoEjeX));
            } else if (dto.getFecha() instanceof java.sql.Timestamp) {
                labels.add(((java.sql.Timestamp) dto.getFecha()).toLocalDateTime().format(formatoEjeX));
            }
            data.add(dto.getTotalMonto());
        });

        reporte.put("graficoLabels", labels);
        reporte.put("graficoDatosVentas", data);
        // Nota: El cálculo de ganancias por día requeriría una query más compleja.
        // Por ahora, se mantiene el dato de ventas.
        reporte.put("graficoDatosGanancias", data);

        return reporte;
    }

    public List<ProductoRentabilidadDTO> generarReporteRentabilidad(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<ProductoRentabilidadDTO> rentabilidad = detalleVentaRepository.obtenerRentabilidadProductos(fechaInicio, fechaFin);
        // Aquí podrías añadir lógica adicional, como calcular el margen porcentual si lo necesitas en el DTO.
        return rentabilidad;
    }

    public List<ServicioRentabilidadDTO> generarReporteRentabilidadServicios(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return detalleVentaRepository.obtenerRentabilidadServicios(fechaInicio, fechaFin);
    }
}