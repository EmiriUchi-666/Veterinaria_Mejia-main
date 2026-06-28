package com.Veterinaria.Mejia.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.Especie;
import com.Veterinaria.Mejia.repository.EspecieRepository;
import com.Veterinaria.Mejia.services.EspecieService;

@Service
public class EspecieServiceImpl implements EspecieService {

    @Autowired
    private EspecieRepository especieRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Especie> listarTodas() {
        // Usa tu JPQL personalizado de ordenamiento alfabético
        return especieRepository.listarTodasOrdenadasJPQL(); 
    }

    @Override
    @Transactional(readOnly = true)
    public Especie buscarPorId(Integer id) {
        return especieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especie no encontrada"));
    }

    @Override
    @Transactional
    public Especie guardar(Especie especie) {
        return especieRepository.save(especie);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Especie especie = buscarPorId(id);
        especieRepository.delete(especie);
    }
}