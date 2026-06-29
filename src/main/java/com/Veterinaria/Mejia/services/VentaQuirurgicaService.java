package com.Veterinaria.Mejia.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.CotizacionQuirurgica;
import com.Veterinaria.Mejia.models.DesgloseItem;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.repository.ProductoRepository;
import com.Veterinaria.Mejia.repository.UsuarioRepository;
import com.Veterinaria.Mejia.repository.VentaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VentaQuirurgicaService {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    @Transactional
    public Venta procesarPagoEncapsulado(CotizacionQuirurgica cotizacion, Authentication authentication) {
        Usuario cajero = usuarioRepository.findByNombreUsuario(authentication.getName())
                .orElseThrow(() -> new SecurityException("Usuario no encontrado."));

        for (DesgloseItem item : cotizacion.getDesgloseItems()) {
            if (item.getProducto() != null) {
                Producto producto = productoRepository.findById(item.getProducto().getId())
                        .orElseThrow(() -> new RuntimeException("Producto de cotización no encontrado."));
                if (producto.getStockTotal().compareTo(item.getCantidad()) < 0) {
                    throw new IllegalStateException("Stock insuficiente para " + producto.getNombre());
                }
                producto.setStockAbierto(producto.getStockAbierto().subtract(item.getCantidad()));
                productoRepository.save(producto);
            }
        }

        Venta venta = new Venta();
        // ... (logic to build the encapsulated sale)

        return ventaRepository.save(venta);
    }
}