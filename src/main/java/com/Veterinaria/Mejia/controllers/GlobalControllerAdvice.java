package com.Veterinaria.Mejia.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.Veterinaria.Mejia.repository.AlertaSistemaRepository;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final AlertaSistemaRepository alertaSistemaRepo;

    @ModelAttribute("totalAlertasGlobal")
    public long getTotalAlertasGlobal() {
        // Esta cuenta se ejecutará en cada petición y estará disponible globalmente en los templates.
        return alertaSistemaRepo.countByLeidaFalse();
    }
}