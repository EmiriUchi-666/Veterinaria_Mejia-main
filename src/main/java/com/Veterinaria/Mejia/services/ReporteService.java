package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        // FASE 5 (corregido): la query JPQL "obtenerGraficoVentasPorFechaJPQL" siempre
        // agrupa por DATE(), sin componente de hora. Cuando el rango es "hoy" se
        // necesita agrupar por HORA, así que se usan las queries nativas específicas
        // (que ya devuelven el label como String con el formato correcto), evitando
        // intentar formatear un LocalDate con un patrón "HH:00" (lo que provocaba
        // "Unsupported field: HourOfDay").
        List<VentasDiaDTO> datosGrafico = rango.equals("hoy")
                ? ventaRepository.obtenerVentasPorHora(fechaInicio)
                : ventaRepository.obtenerVentasPorDia(fechaInicio);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        datosGrafico.forEach(dto -> {
            labels.add(dto.getFecha() != null ? dto.getFecha().toString() : "");
            data.add(dto.getTotalMonto() != null ? dto.getTotalMonto() : BigDecimal.ZERO);
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