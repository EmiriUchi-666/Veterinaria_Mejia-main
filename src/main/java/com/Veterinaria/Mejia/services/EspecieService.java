package com.Veterinaria.Mejia.services;

import java.util.List;

import com.Veterinaria.Mejia.models.Especie;

public interface EspecieService {
    // 
    List<Especie> listarTodas();
    
    Especie buscarPorId(Integer id);
    Especie guardar(Especie especie);
    void eliminar(Integer id);
}