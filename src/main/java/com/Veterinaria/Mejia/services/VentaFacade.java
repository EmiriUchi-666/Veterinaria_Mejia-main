package com.Veterinaria.Mejia.services;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.Veterinaria.Mejia.dto.VentaRequestDTO;
import com.Veterinaria.Mejia.models.CotizacionQuirurgica;
import com.Veterinaria.Mejia.models.Venta;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VentaFacade {

    private final VentaConsultaService ventaConsultaService;
    private final VentaCreacionService ventaCreacionService;
    private final VentaQuirurgicaService ventaQuirurgicaService;
    private final VentaAnulacionService ventaAnulacionService;

    public List<Venta> listarUltimas10VentasConDetalles() {
        return ventaConsultaService.listarUltimas10VentasConDetalles();
    }

    public Venta buscarPorId(Integer id) {
        return ventaConsultaService.buscarPorId(id);
    }

    public Venta procesarVentaTransaccional(VentaRequestDTO request, Authentication authentication) {
        return ventaCreacionService.procesarVentaTransaccional(request, authentication);
    }

    public Venta procesarPagoEncapsulado(CotizacionQuirurgica cotizacion, Authentication authentication) {
        return ventaQuirurgicaService.procesarPagoEncapsulado(cotizacion, authentication);
    }

    public void anularVenta(Integer id) {
        ventaAnulacionService.anularVenta(id);
    }
}