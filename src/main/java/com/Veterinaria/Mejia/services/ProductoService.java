package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.Merma;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.repository.MermaRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MermaRepository mermaRepository; // INYECTAMOS EL NUEVO REPOSITORIO

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> buscarInventario(String nombre, Integer categoriaId, Integer especieId) {
        return productoRepository.buscarYFiltrarInventarioJPQL(nombre, categoriaId, especieId);
    }

    public List<Producto> listarStockCritico() {
        return productoRepository.buscarProductosStockCriticoJPQL();
    }

    @Transactional
    public Producto guardarProductoNuevo(Producto producto) {
        producto.setEstado(true);
        if (producto.getStockTotal().compareTo(new BigDecimal("99.00")) > 0) {
            throw new IllegalArgumentException("Regla de negocio: El stock inicial no puede exceder las 99 unidades/kg/litros.");
        }
        return productoRepository.save(producto);
    }

    @Transactional
    public void modificarEstado(Integer idProducto, boolean nuevoEstado) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));
        
        if (!nuevoEstado && producto.getStockTotal().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Bloqueo de seguridad: No puedes inactivar un producto que aún tiene stock físico.");
        }
        
        producto.setEstado(nuevoEstado);
        productoRepository.save(producto);
    }

    // ACTUALIZADO: GESTIÓN DE PÉRDIDAS EN LA NUEVA TABLA
    @Transactional
    public void reportarMermaDesecho(Integer idProducto, BigDecimal cantidadDesechada) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));

        if (producto.getStockTotal().compareTo(cantidadDesechada) < 0) {
            throw new IllegalArgumentException("No hay suficiente stock en sistema para desechar esta cantidad.");
        }

        // 1. Restamos del inventario
        producto.setStockTotal(producto.getStockTotal().subtract(cantidadDesechada));
        productoRepository.save(producto);

        // 2. Calculamos el dinero perdido (Cantidad * Precio Inversión)
        BigDecimal perdidaEconomica = cantidadDesechada.multiply(producto.getPrecioInversion());
        
        // 3. Registramos la pérdida formalmente en la tabla Mermas
        Merma nuevaMerma = Merma.builder()
                .producto(producto)
                .cantidad(cantidadDesechada)
                .perdidaEconomica(perdidaEconomica)
                .fechaRegistro(LocalDateTime.now())
                .build();
                
        mermaRepository.save(nuevaMerma);
    }

    // ==========================================
    // MÉTODOS PARA EL DASHBOARD DE IA
    // ==========================================

    /** Cuenta los productos con stock por debajo del mínimo. */
    public long contarStockBajo() {
        return productoRepository.buscarProductosStockCriticoJPQL().size();
    }

    /** Retorna la lista de productos con stock por debajo del mínimo. */
    public java.util.List<com.Veterinaria.Mejia.models.Producto> obtenerStockBajo() {
        return productoRepository.buscarProductosStockCriticoJPQL();
    }
}
