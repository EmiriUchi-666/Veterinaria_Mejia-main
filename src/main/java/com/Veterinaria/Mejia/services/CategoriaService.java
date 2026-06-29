package com.Veterinaria.Mejia.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Veterinaria.Mejia.models.Categoria;
import com.Veterinaria.Mejia.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> buscarPorNombre(String nombre) {
        return categoriaRepository.buscarPorNombreJPQL(nombre);
    }

    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }
}