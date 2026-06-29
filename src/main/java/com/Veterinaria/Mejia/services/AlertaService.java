package com.Veterinaria.Mejia.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.AlertaSistema;
import com.Veterinaria.Mejia.models.Cita;
import com.Veterinaria.Mejia.models.HistorialVacuna;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.repository.AlertaSistemaRepository;
import com.Veterinaria.Mejia.repository.CitaRepository;
import com.Veterinaria.Mejia.repository.HistorialVacunaRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

/**
 * FASE 8: Servicio de Alertas Proactivas.
 * Incluye un método @Scheduled para generar alertas diarias de forma automática.
 */
@Service
@RequiredArgsConstructor
public class AlertaService {

    private final HistorialVacunaRepository vacunaRepo;
    private final CitaRepository citaRepo;
    private final ProductoRepository productoRepo;
    private final AlertaSistemaRepository alertaSistemaRepo;

    /**
     * Alerta 1: Vacunas próximas a vencer (en los siguientes 30 días).
     */
    public List<HistorialVacuna> getAlertasVacunasPendientes() {
        LocalDate hoy = LocalDate.now();
        return vacunaRepo.findVacunasPendientes(hoy, hoy.plusDays(30));
    }

    /**
     * Alerta 2: Citas de hoy que aún están en estado 'Pendiente'.
     */
    public List<Cita> getAlertasCitasNoAtendidas() {
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = LocalDate.now().atTime(23, 59, 59);
        return citaRepo.findCitasPendientes(inicioHoy, finHoy);
    }

    /**
     * Alerta 3: Productos cuyo stock actual es menor o igual al stock mínimo.
     */
    public List<Producto> getAlertasStockBajo() {
        return productoRepo.buscarProductosStockCriticoJPQL();
    }

    /**
     * Alerta 4: Productos con baja rotación (sin ventas en los últimos 60 días).
     */
    public List<Producto> getAlertasBajaRotacion() {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(60);
        return productoRepo.findProductosSinVentasDesde(fechaLimite);
    }

    /**
     * Tarea programada que se ejecuta todos los días a las 8:00 AM.
     * Calcula todas las alertas del sistema y las guarda en la tabla `alertas_sistema`.
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    @SuppressWarnings("null")
    public void generarAlertasDiarias() {
        // 1. Limpiar alertas no leídas del día anterior para no acumular.
        alertaSistemaRepo.deleteUnreadAlerts();

        // 2. Generar Alertas de Vacunas
        getAlertasVacunasPendientes().forEach(v -> {
            String mensaje = String.format("Refuerzo de %s para %s vence el %s.",
                    v.getNombreVacuna(),
                    v.getPaciente().getNombre(),
                    v.getFechaProximoRefuerzo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            AlertaSistema alerta = AlertaSistema.builder()
                    .tipo(AlertaSistema.TipoAlerta.VACUNA_VENCE)
                    .mensaje(mensaje)
                    .entidadId(v.getPaciente().getId())
                    .entidadTipo("Paciente")
                    .fechaGenerada(LocalDateTime.now())
                    .build();
            alertaSistemaRepo.save(alerta);
        });

        // 3. Generar Alertas de Stock Bajo
        getAlertasStockBajo().forEach(p -> {
            String mensaje = String.format("Stock bajo para %s. Actual: %.2f, Mínimo: %.2f.",
                    p.getNombre(), p.getStockTotal(), p.getStockMinimo());
            AlertaSistema alerta = AlertaSistema.builder()
                    .tipo(AlertaSistema.TipoAlerta.STOCK_BAJO)
                    .mensaje(mensaje)
                    .entidadId(p.getId())
                    .entidadTipo("Producto")
                    .fechaGenerada(LocalDateTime.now())
                    .build();
            alertaSistemaRepo.save(alerta);
        });

        // 4. Generar Alertas de Baja Rotación
        getAlertasBajaRotacion().forEach(p -> {
            String mensaje = String.format("Baja rotación: %s no se vende hace más de 60 días.", p.getNombre());
            AlertaSistema alerta = AlertaSistema.builder()
                    .tipo(AlertaSistema.TipoAlerta.BAJA_ROTACION)
                    .mensaje(mensaje)
                    .entidadId(p.getId())
                    .entidadTipo("Producto")
                    .fechaGenerada(LocalDateTime.now())
                    .build();
            alertaSistemaRepo.save(alerta);
        });

        // La alerta de citas no atendidas se puede seguir manejando en tiempo real en el dashboard si se prefiere.
    }
}