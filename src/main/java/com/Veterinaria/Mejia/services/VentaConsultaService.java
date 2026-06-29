package com.Veterinaria.Mejia.services;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.repository.VentaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VentaConsultaService {

    private final VentaRepository ventaRepository;

    public List<Venta> listarUltimas10VentasConDetalles() {
        return ventaRepository.findTop10WithDetails(PageRequest.of(0, 10));
    }

    public Venta buscarPorId(Integer id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }
}