package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.CotizacionQuirurgica;
import com.Veterinaria.Mejia.models.DesgloseItem;
import com.Veterinaria.Mejia.models.DetalleVenta;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.models.Servicio;
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

        // 2. Lógica EXTERNA (SUNAT/Cliente): Generar una venta con un único item
        Venta venta = new Venta();
        venta.setUsuario(cajero);
        venta.setCliente(cotizacion.getCliente());
        venta.setFechaEmision(LocalDateTime.now());
        venta.setTotalVenta(cotizacion.getTotal());
        venta.setTipoPago("Tarjeta"); // Asumimos un tipo de pago
        venta.setEstado(true);
        venta.setTipoComprobante("Factura"); // O Boleta según el cliente
        venta.setSerie("F001");
        Integer ultimoCorrelativo = ventaRepository.obtenerMaximoCorrelativoJPQL(venta.getSerie());
        venta.setCorrelativo(ultimoCorrelativo == null ? 1 : ultimoCorrelativo + 1);

        DetalleVenta detalleUnico = new DetalleVenta();
        detalleUnico.setVenta(venta);
        detalleUnico.setCantidad(BigDecimal.ONE);
        detalleUnico.setPrecioUnitario(cotizacion.getTotal());
        detalleUnico.setSubtotal(cotizacion.getTotal());

        // Crear un Servicio "dummy" para el detalle
        Servicio servicioQuirurgico = new Servicio();
        servicioQuirurgico.setNombreServicio("SERVICIO MEDICO QUIRURGico INTEGRAL");
        detalleUnico.setServicio(servicioQuirurgico);
        venta.setDetallesVentas(new ArrayList<>(java.util.List.of(detalleUnico)));

        return ventaRepository.save(venta);
    }
}