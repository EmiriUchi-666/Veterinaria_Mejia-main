package com.Veterinaria.Mejia.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.Cliente;
import com.Veterinaria.Mejia.repository.ClienteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repo; // Spring lo inyecta automáticamente gracias a @RequiredArgsConstructor

    public List<Cliente> findAll() { 
        return repo.findAll(); 
    }

    public Optional<Cliente> findById(Integer id) { 
        return repo.findById(id); 
    }

    @Transactional
    public Cliente save(Cliente c) {
        // Validación limpia usando tu consulta explícita en JPQL
        if (c.getNumeroDocumento() != null && !c.getNumeroDocumento().trim().isEmpty()) {
            if (repo.existsByNumeroDocumento(c.getNumeroDocumento())) {
                throw new RuntimeException("El número de documento ya se encuentra registrado en el sistema.");
            }
        }
        return repo.save(c);
    }

    @Transactional
    public void deleteById(Integer id) { 
        repo.deleteById(id); 
    }

    public List<Cliente> buscar(String nombre) { 
        return repo.findByNombreContainingJPQL(nombre); 
    }
}