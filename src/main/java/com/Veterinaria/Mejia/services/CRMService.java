package com.Veterinaria.Mejia.services;

import com.Veterinaria.Mejia.models.*;
import com.Veterinaria.Mejia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servicio CRM: calcula métricas de clientes, los clasifica en segmentos
 * (VIP, Frecuente, Ocasional, Inactivo) y detecta clientes en riesgo de abandono.
 */
@Service
public class CRMService {

    @Autowired
    private ClienteMetricaRepository metricaRepo;

    @Autowired
    private SegmentoClienteRepository segmentoRepo;

    @Autowired
    private VentaRepository ventaRepo;

    @Autowired
    private ClienteRepository clienteRepo;

    /**
     * Calcula y persiste las métricas de un cliente específico.
     *
     * @param clienteId ID del cliente
     * @return ClienteMetrica actualizada
     */
    @Transactional
    public ClienteMetrica calcularMetricasCliente(Integer clienteId) {
        Cliente cliente = clienteRepo.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + clienteId));

        // Obtener historial de ventas del cliente
        List<Venta> ventas = ventaRepo.buscarHistorialPorClienteJPQL(clienteId);

        // Calcular métricas
        int totalVisitas = ventas.size();
        double gastoTotal = ventas.stream()
                .filter(v -> v.getEstado() != null && v.getEstado())
                .mapToDouble(v -> v.getTotalVenta() != null ? v.getTotalVenta().doubleValue() : 0.0)
                .sum();
        double gastoPromedio = totalVisitas > 0 ? gastoTotal / totalVisitas : 0.0;

        LocalDateTime ultimaVisita = ventas.isEmpty() ? null : ventas.get(0).getFechaEmision();
        int diasSinVisita = 0;
        if (ultimaVisita != null) {
            diasSinVisita = (int) ChronoUnit.DAYS.between(ultimaVisita.toLocalDate(), LocalDate.now());
        }

        // Buscar o crear métricas
        ClienteMetrica metrica = metricaRepo.findByClienteId(clienteId)
                .orElse(new ClienteMetrica());

        metrica.setCliente(cliente);
        metrica.setFechaCalculo(LocalDate.now());
        metrica.setTotalVisitas(totalVisitas);
        metrica.setGastoTotal(gastoTotal);
        metrica.setGastoPromedio(gastoPromedio);
        metrica.setUltimaVisita(ultimaVisita);
        metrica.setDiasSinVisita(diasSinVisita);

        // Clasificar segmento
        SegmentoCliente segmento = clasificarSegmento(totalVisitas, gastoTotal, diasSinVisita);
        metrica.setSegmento(segmento);

        return metricaRepo.save(metrica);
    }

    /**
     * Determina el segmento de un cliente según sus métricas.
     * Reglas: VIP > Frecuente > Ocasional > Inactivo
     */
    private SegmentoCliente clasificarSegmento(int visitas, double gasto, int diasInactivo) {
        if (diasInactivo > 180) {
            return segmentoRepo.findByNombre("Inactivo").orElse(null);
        }
        // Visitas equivalentes al año (aproximación)
        int visitasAnio = (int) (visitas * (365.0 / Math.max(diasInactivo + 1, 365)));

        if (visitasAnio >= 12 && gasto >= 500.0) {
            return segmentoRepo.findByNombre("VIP").orElse(null);
        } else if (visitasAnio >= 6 && gasto >= 200.0) {
            return segmentoRepo.findByNombre("Frecuente").orElse(null);
        } else if (visitas >= 2) {
            return segmentoRepo.findByNombre("Ocasional").orElse(null);
        }
        return segmentoRepo.findByNombre("Inactivo").orElse(null);
    }

    /**
     * Recalcula las métricas de TODOS los clientes del sistema.
     * Útil para ejecución programada o manual desde el dashboard.
     */
    @Transactional
    public void recalcularTodosLosClientes() {
        clienteRepo.findAll().forEach(c -> {
            try {
                calcularMetricasCliente(c.getId());
            } catch (Exception e) {
                // Continuar con el siguiente cliente si uno falla
                System.err.println("Error al calcular métricas del cliente " + c.getId() + ": " + e.getMessage());
            }
        });
    }

    /** Lista clientes sin visitar en los últimos N días. */
    public List<ClienteMetrica> obtenerClientesInactivos(Integer diasSinVisita) {
        return metricaRepo.findClientesInactivos(diasSinVisita);
    }

    /** Top N clientes por gasto total. */
    public List<ClienteMetrica> obtenerTopClientes(int limite) {
        return metricaRepo.findTopClientesByGasto(PageRequest.of(0, limite));
    }

    /** Lista clientes de un segmento específico. */
    public List<ClienteMetrica> obtenerPorSegmento(String segmento) {
        return metricaRepo.findBySegmentoNombre(segmento);
    }

    /** Métricas de un cliente específico. */
    public ClienteMetrica obtenerMetricas(Integer clienteId) {
        return metricaRepo.findByClienteId(clienteId).orElse(null);
    }

    /** Lista todos los segmentos disponibles. */
    public List<SegmentoCliente> listarSegmentos() {
        return segmentoRepo.findAll();
    }
}
