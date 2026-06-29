package com.Veterinaria.Mejia.services;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.Servicio;
import com.Veterinaria.Mejia.repository.ServicioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;

    // 1. BUSCADOR INTELIGENTE (Para tu panel de Gestión de Servicios)
    // Reemplaza a listarTodos() para no sobrecargar la memoria cargando todo de golpe
    public List<Servicio> buscarServicios(String nombre) {
        return servicioRepository.buscarYFiltrarServiciosJPQL(nombre);
    }

    // 2. LISTAR SOLO ACTIVOS (Para el selector en tu Punto de Venta - POS)
    // Garantiza que el cajero solo pueda vender servicios habilitados
    public List<Servicio> listarActivosPOS() {
        return servicioRepository.listarServiciosActivosJPQL();
    }

    public Servicio buscarPorId(Integer id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El servicio solicitado no existe."));
    }

    // REGISTRO DE NUEVO SERVICIO
    @Transactional
    public Servicio guardarServicioNuevo(Servicio servicio) {
        servicio.setEstado(true); // Forzamos a que nazca siempre activo
        return servicioRepository.save(servicio);
    }

    // =========================================================================
    // BORRADO LÓGICO: Solo cambia el estado, NUNCA borra de la base de datos
    // =========================================================================
    @Transactional
    public void modificarEstado(Integer idServicio, boolean nuevoEstado) {
        Servicio servicio = buscarPorId(idServicio);
        
        // Al pasar a inactivo (false), desaparece del POS pero sigue existiendo para reportes pasados
        servicio.setEstado(nuevoEstado);
        servicioRepository.save(servicio);
    }

    @Transactional
    public void eliminarFisicamente(Integer id) {
        try {
            servicioRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // Si MySQL arroja error de llave foránea (Foreign Key constraint)
            throw new RuntimeException("No se puede eliminar el servicio porque ya forma parte del historial de ventas. Por favor, utilice la opción de desactivar.");
        }
    }
}