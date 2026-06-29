package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.dto.ItemCarritoDTO;
import com.Veterinaria.Mejia.models.DetalleVenta;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;

    @Transactional(rollbackFor = Exception.class)
    public Producto descontarStockDeProducto(ItemCarritoDTO item) {
        Producto prod = productoRepository.findById(item.getIdItem())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getIdItem()));

        boolean esFraccionado = "PRODUCTO_FRACCIONADO".equalsIgnoreCase(item.getTipo());

        if (esFraccionado) {
            if (!Boolean.TRUE.equals(prod.getPermiteFraccionamiento())) {
                throw new IllegalArgumentException("El producto '" + prod.getNombre() + "' no permite venta fraccionada.");
            }
            BigDecimal cantReq = item.getCantidad();
            while (prod.getStockAbierto().compareTo(cantReq) < 0) {
                if (prod.getContenidoPorEnvase() == null || prod.getContenidoPorEnvase().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalStateException("Configuración de envase inválida para el producto '" + prod.getNombre() + "'. Imposible fraccionar.");
                }
                if (prod.getStockCerrado() == null || prod.getStockCerrado() <= 0) {
                    throw new RuntimeException("Stock insuficiente para venta suelta de: " + prod.getNombre());
                }
                prod.setStockCerrado(prod.getStockCerrado() - 1);
                prod.setStockAbierto(prod.getStockAbierto().add(prod.getContenidoPorEnvase()));
            }
            prod.setStockAbierto(prod.getStockAbierto().subtract(cantReq));
        } else { // Venta de producto entero
            int cantidadEntera = item.getCantidad().intValue();
            if (prod.getStockCerrado() < cantidadEntera) {
                throw new RuntimeException("Stock insuficiente de '" + prod.getNombre()
                        + "'. Disponible: " + prod.getStockCerrado());
            }
            prod.setStockCerrado(prod.getStockCerrado() - cantidadEntera);
        }
        
        return productoRepository.save(prod);
    }

    @Transactional(rollbackFor = Exception.class)
    public Producto devolverStockDeProducto(DetalleVenta detalle) {
        if (detalle.getProducto() == null) {
            // No hay producto en este detalle de venta, no hay nada que devolver.
            return null;
        }

        Producto producto = productoRepository.findById(detalle.getProducto().getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado para devolución de stock: " + detalle.getProducto().getId()));

        // Si el producto permite fraccionamiento, la forma más segura de devolver
        // es al stock abierto. Si no, se devuelve a las unidades cerradas.
        if (Boolean.TRUE.equals(producto.getPermiteFraccionamiento())) {
            producto.setStockAbierto(producto.getStockAbierto().add(detalle.getCantidad()));
        } else {
            // Si no es fraccionable, se vendió como una unidad entera.
            int cantidadDevuelta = detalle.getCantidad().intValue();
            producto.setStockCerrado(producto.getStockCerrado() + cantidadDevuelta);
        }

        return productoRepository.save(producto);
    }
}