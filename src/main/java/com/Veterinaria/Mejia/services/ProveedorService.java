package com.Veterinaria.Mejia.services;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.Proveedor;
import com.Veterinaria.Mejia.repository.ProveedorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public List<Proveedor> listarTodos() {
        return proveedorRepository.findAll();
    }

    public Proveedor buscarPorId(Integer id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado en el sistema."));
    }

    public Proveedor buscarPorRuc(String ruc) {
        return proveedorRepository.buscarPorRucJPQL(ruc) // Asegúrate de tener este método en el repository
                .orElseThrow(() -> new RuntimeException("No se encontró ningún proveedor con el RUC indicado."));
    }

    // REGISTRO DE NUEVO PROVEEDOR
    @Transactional
    public Proveedor guardar(Proveedor proveedor) {
        // Regla de Negocio: Evitar duplicidad de RUC en la base de datos
        if (proveedor.getRuc() != null && !proveedor.getRuc().trim().isEmpty()) {
            Optional<Proveedor> existente = proveedorRepository.buscarPorRucJPQL(proveedor.getRuc());
            
            // Si el RUC existe y le pertenece a un proveedor diferente al que estamos editando
            if (existente.isPresent() && !existente.get().getId().equals(proveedor.getId())) {
                throw new IllegalArgumentException("Error: El RUC ingresado ya está registrado a nombre de otra empresa proveedora.");
            }
        }
        
        return proveedorRepository.save(proveedor);
    }

    @Transactional

    public void modificarEstado(Integer id, boolean nuevoEstado) {
        Proveedor proveedor = buscarPorId(id);
        proveedor.setEstado(nuevoEstado);
        proveedorRepository.save(proveedor);
    }

    @Transactional
    public void eliminar(Integer id) {
        try {
            proveedorRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("No se puede eliminar el proveedor porque ya forma parte del historial de abastecimiento. Por favor, utilice la opción de dejar inactivo.");
        }
    }
}