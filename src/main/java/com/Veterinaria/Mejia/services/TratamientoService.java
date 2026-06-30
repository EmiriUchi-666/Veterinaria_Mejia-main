package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.DetalleTratamiento;
import com.Veterinaria.Mejia.models.HistoriaClinica;
import com.Veterinaria.Mejia.models.Paciente;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.models.Tratamiento;
import com.Veterinaria.Mejia.repository.DetalleTratamientoRepository;
import com.Veterinaria.Mejia.repository.HistoriaClinicaRepository;
import com.Veterinaria.Mejia.repository.PacienteRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;
import com.Veterinaria.Mejia.repository.TratamientoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para la gestión de tratamientos médicos con trazabilidad de medicamentos.
 * Descuenta automáticamente el stock al registrar un detalle de tratamiento.
 */
@Service
@RequiredArgsConstructor
public class TratamientoService {

    private final TratamientoRepository tratamientoRepo;
    private final DetalleTratamientoRepository detalleTratamientoRepo;
    private final HistoriaClinicaRepository historiaClinicaRepo;
    private final PacienteRepository pacienteRepo;
    private final ProductoRepository productoRepo;

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

            if (producto.getStockTotal() == null || producto.getStockTotal().compareTo(detalle.getCantidadUsada()) < 0) {
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

            // --- LÓGICA DE DESCUENTO DE STOCK CORREGIDA ---
            if (Boolean.TRUE.equals(producto.getPermiteFraccionamiento())) {
                BigDecimal cantidadRequerida = detalle.getCantidadUsada();
                // Si no hay suficiente en el stock abierto, se abre un envase cerrado.
                while (producto.getStockAbierto().compareTo(cantidadRequerida) < 0) {
                    if (producto.getContenidoPorEnvase() == null || producto.getContenidoPorEnvase().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalStateException("Configuración de envase inválida para '" + producto.getNombre() + "'.");
                    }
                    if (producto.getStockCerrado() == null || producto.getStockCerrado() <= 0) {
                        throw new RuntimeException("Stock insuficiente para fraccionar: " + producto.getNombre());
                    }
                    producto.setStockCerrado(producto.getStockCerrado() - 1);
                    producto.setStockAbierto(producto.getStockAbierto().add(producto.getContenidoPorEnvase()));
                }
                producto.setStockAbierto(producto.getStockAbierto().subtract(cantidadRequerida));
            } else {
                // Para productos no fraccionables, se descuenta de las unidades enteras.
                int cantidadEntera = detalle.getCantidadUsada().intValue();
                if (producto.getStockCerrado() < cantidadEntera) {
                    throw new RuntimeException("Stock insuficiente de unidades cerradas para: " + producto.getNombre());
                }
                producto.setStockCerrado(producto.getStockCerrado() - cantidadEntera);
            }
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
     * Registra un nuevo tratamiento general no asociado a una historia clínica específica.
     *
     * @param pacienteId ID del paciente al que se asocia el tratamiento (opcional)
     * @param diagnostico Descripción del diagnóstico
     * @param observaciones Observaciones adicionales
     * @param detalles Lista de detalles (producto + cantidad)
     * @return El tratamiento guardado
     * @throws IllegalArgumentException si algún producto no tiene stock suficiente
     */
    @Transactional
    public Tratamiento registrarTratamientoGeneral(Integer pacienteId,
                                                   String diagnostico,
                                                   String observaciones,
                                                   List<DetalleTratamiento> detalles) {

        // 1. Validar stock de todos los productos ANTES de guardar nada
        for (DetalleTratamiento detalle : detalles) {
            Producto producto = productoRepo.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalle.getProducto().getId()));

            if (producto.getStockTotal() == null || producto.getStockTotal().compareTo(detalle.getCantidadUsada()) < 0) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para '" + producto.getNombre() + "'. " +
                        "Stock disponible: " + producto.getStockTotal() + " | Requerido: " + detalle.getCantidadUsada()
                );
            }
        }

        // 2. Crear el tratamiento
        Tratamiento tratamiento = new Tratamiento();
        if (pacienteId != null) {
            Paciente paciente = pacienteRepo.findById(pacienteId)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + pacienteId));
            tratamiento.setPacienteTratado(paciente);
        }
        tratamiento.setFechaInicio(LocalDate.now());
        tratamiento.setDiagnostico(diagnostico);
        tratamiento.setObservaciones(observaciones);
        tratamiento.setEstado(Tratamiento.EstadoTratamiento.Activo);
        Tratamiento tratamientoGuardado = tratamientoRepo.save(tratamiento);

        // 3. Procesar cada detalle: guardar + descontar stock
        for (DetalleTratamiento detalle : detalles) {
            Producto producto = productoRepo.findById(detalle.getProducto().getId()).get();

            if (Boolean.TRUE.equals(producto.getPermiteFraccionamiento())) {
                BigDecimal cantidadRequerida = detalle.getCantidadUsada();
                while (producto.getStockAbierto().compareTo(cantidadRequerida) < 0) {
                    if (producto.getStockCerrado() == null || producto.getStockCerrado() <= 0) {
                        throw new RuntimeException("Stock insuficiente para fraccionar: " + producto.getNombre());
                    }
                    producto.setStockCerrado(producto.getStockCerrado() - 1);
                    producto.setStockAbierto(producto.getStockAbierto().add(producto.getContenidoPorEnvase()));
                }
                producto.setStockAbierto(producto.getStockAbierto().subtract(cantidadRequerida));
            } else {
                int cantidadEntera = detalle.getCantidadUsada().intValue();
                producto.setStockCerrado(producto.getStockCerrado() - cantidadEntera);
            }
            productoRepo.save(producto);

            detalle.setTratamiento(tratamientoGuardado);
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

    public List<Tratamiento> listarTodos() {
        return tratamientoRepo.findAllByOrderByFechaInicioDesc();
    }
}
