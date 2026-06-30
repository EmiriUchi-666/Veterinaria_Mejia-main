package com.Veterinaria.Mejia.services;

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
            Producto producto = productoRepo.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalle.getProducto().getId()));
            producto.descontarStock(detalle.getCantidadUsada()); // Delegar la lógica de descuento al producto
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

        // 1. Validar y obtener el paciente.
        if (pacienteId == null) {
            throw new IllegalArgumentException("Se debe seleccionar un paciente para registrar un tratamiento.");
        }
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + pacienteId));
        // A patient can have multiple histories. We get the list and pick the most recent one if it exists.
        List<HistoriaClinica> historias = historiaClinicaRepo.findByPacienteIdOrderByFechaAtencionDesc(pacienteId);
        HistoriaClinica historiaReciente = historias.isEmpty() ? null : historias.get(0);

        // 2. Crear el tratamiento y asociarlo a la historia clínica si existe.
        Tratamiento tratamiento = new Tratamiento();
        tratamiento.setHistoriaClinica(historiaReciente);
        tratamiento.setFechaInicio(LocalDate.now());
        tratamiento.setPacienteTratado(paciente); // Associate treatment directly with the patient
        tratamiento.setDiagnostico(diagnostico);
        tratamiento.setObservaciones(observaciones);
        tratamiento.setEstado(Tratamiento.EstadoTratamiento.Activo);
        Tratamiento tratamientoGuardado = tratamientoRepo.save(tratamiento);

        // 3. Procesar cada detalle: guardar + descontar stock
        for (DetalleTratamiento detalle : detalles) {
            Producto producto = productoRepo.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalle.getProducto().getId()));
            producto.descontarStock(detalle.getCantidadUsada()); // Delegar la lógica de descuento al producto
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

    public Tratamiento obtenerPorId(Integer id) {
        return tratamientoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado"));
    }

    public List<Tratamiento> listarTodos() {
        return tratamientoRepo.findAllByOrderByFechaInicioDesc();
    }
}
