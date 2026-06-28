package com.Veterinaria.Mejia.services;

import com.Veterinaria.Mejia.models.*;
import com.Veterinaria.Mejia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para la gestión de tratamientos médicos con trazabilidad de medicamentos.
 * Descuenta automáticamente el stock al registrar un detalle de tratamiento.
 */
@Service
public class TratamientoService {

    @Autowired
    private TratamientoRepository tratamientoRepo;

    @Autowired
    private DetalleTratamientoRepository detalleTratamientoRepo;

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepo;

    @Autowired
    private ProductoRepository productoRepo;

    /**
     * Registra un nuevo tratamiento y descuenta el stock de cada producto usado.
     *
     * @param historiaClinicaId  ID de la historia clínica asociada
     * @param diagnostico        Descripción del diagnóstico
     * @param observaciones      Observaciones adicionales
     * @param detalles           Lista de detalles (producto + cantidad)
     * @return El tratamiento guardado
     * @throws IllegalArgumentException si algún producto no tiene stock suficiente
     */
    @Transactional
    public Tratamiento registrarTratamiento(Integer historiaClinicaId,
                                            String diagnostico,
                                            String observaciones,
                                            List<DetalleTratamiento> detalles) {

        // 1. Validar que la historia clínica exista
        HistoriaClinica historia = historiaClinicaRepo.findById(historiaClinicaId)
                .orElseThrow(() -> new RuntimeException("Historia clínica no encontrada con ID: " + historiaClinicaId));

        // 2. Validar stock de todos los productos ANTES de guardar nada
        for (DetalleTratamiento detalle : detalles) {
            Producto producto = productoRepo.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalle.getProducto().getId()));

            if (producto.getStockTotal().compareTo(detalle.getCantidadUsada()) < 0) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para '" + producto.getNombre() + "'. " +
                        "Stock disponible: " + producto.getStockTotal() + " | Requerido: " + detalle.getCantidadUsada()
                );
            }
        }

        // 3. Crear el tratamiento
        Tratamiento tratamiento = new Tratamiento();
        tratamiento.setHistoriaClinica(historia);
        tratamiento.setFechaInicio(LocalDate.now());
        tratamiento.setDiagnostico(diagnostico);
        tratamiento.setObservaciones(observaciones);
        tratamiento.setEstado(Tratamiento.EstadoTratamiento.Activo);
        Tratamiento tratamientoGuardado = tratamientoRepo.save(tratamiento);

        // 4. Procesar cada detalle: guardar + descontar stock
        for (DetalleTratamiento detalle : detalles) {
            Producto producto = productoRepo.findById(detalle.getProducto().getId()).get();

            // Descontar stock
            BigDecimal nuevoStock = producto.getStockTotal().subtract(detalle.getCantidadUsada());
            producto.setStockTotal(nuevoStock);
            productoRepo.save(producto);

            // Guardar detalle vinculado al tratamiento
            detalle.setTratamiento(tratamientoGuardado);
            detalle.setProducto(producto);
            detalle.setFechaAplicacion(LocalDateTime.now());
            detalleTratamientoRepo.save(detalle);
        }

        return tratamientoGuardado;
    }

    /**
     * Cambia el estado de un tratamiento (Activo → Completado o Suspendido).
     */
    @Transactional
    public void cambiarEstado(Integer tratamientoId, Tratamiento.EstadoTratamiento nuevoEstado) {
        Tratamiento t = tratamientoRepo.findById(tratamientoId)
                .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));
        if (nuevoEstado == Tratamiento.EstadoTratamiento.Completado) {
            t.setFechaFin(LocalDate.now());
        }
        t.setEstado(nuevoEstado);
        tratamientoRepo.save(t);
    }

    /** Obtiene todos los tratamientos de un paciente específico. */
    public List<Tratamiento> listarPorPaciente(Integer pacienteId) {
        return tratamientoRepo.findByHistoriaClinicaPacienteIdOrderByFechaInicioDesc(pacienteId);
    }

    /** Obtiene los detalles de un tratamiento. */
    public List<DetalleTratamiento> obtenerDetalles(Integer tratamientoId) {
        return detalleTratamientoRepo.findByTratamientoId(tratamientoId);
    }

    /** Cuenta los tratamientos activos para el dashboard IA. */
    public long contarTratamientosActivos() {
        return tratamientoRepo.contarTratamientosActivos();
    }

    public Tratamiento obtenerPorId(Integer id) {
        return tratamientoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));
    }
}
