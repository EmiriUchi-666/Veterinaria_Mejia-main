package com.Veterinaria.Mejia.services;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.Servicio;
import com.Veterinaria.Mejia.repository.DetalleVentaRepository;
import com.Veterinaria.Mejia.repository.ServicioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    /**
     * Guarda un servicio, ya sea para crearlo o actualizarlo.
     * @param servicio El servicio a guardar.
     * @return El servicio guardado.
     */
    @Transactional
    public Servicio guardarServicio(Servicio servicio) {
        // Lógica para manejar tanto creación como edición
        return servicioRepository.save(servicio);
    }

    /**
     * Busca un servicio por su ID.
     * @param id El ID del servicio.
     * @return El servicio encontrado.
     * @throws RuntimeException si el servicio no existe.
     */
    public Servicio buscarPorId(Integer id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
    }

    /**
     * Busca servicios por nombre. Si el nombre es nulo, devuelve todos.
     * @param nombre El nombre a buscar (puede ser parcial).
     * @return Una lista de servicios.
     */
    public List<Servicio> buscarServicios(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return servicioRepository.findAll();
        }
        return servicioRepository.findByNombreServicioContainingIgnoreCase(nombre);
    }

    /**
     * Modifica el estado (activo/inactivo) de un servicio.
     * @param id El ID del servicio.
     * @param nuevoEstado El nuevo estado (true para activo, false para inactivo).
     */
    @Transactional
    public void modificarEstado(Integer id, boolean nuevoEstado) {
        Servicio servicio = buscarPorId(id);
        servicio.setEstado(nuevoEstado);
        servicioRepository.save(servicio);
    }

    /**
     * Elimina un servicio físicamente de la base de datos.
     * @param id El ID del servicio a eliminar.
     * @throws RuntimeException si el servicio está asociado a una venta.
     */
    @Transactional
    public void eliminarFisicamente(Integer id) {
        if (detalleVentaRepository.countByServicioId(id) > 0) {
            throw new DataIntegrityViolationException("No se puede eliminar el servicio porque ya ha sido vendido al menos una vez.");
        }
        servicioRepository.deleteById(id);
    }

    public List<Servicio> listarActivosPOS() {
        return servicioRepository.findByEstadoTrue();
    }
}