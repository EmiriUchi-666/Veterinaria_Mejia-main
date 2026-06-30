package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.DetalleIngresoStock;
import com.Veterinaria.Mejia.models.IngresoStock;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.IngresoStockRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IngresoStockService {

    private final IngresoStockRepository ingresoStockRepository;

    private final ProductoRepository productoRepository;

    private final CajaService cajaService;

    // ==========================================
    // 1. LISTAR HISTORIAL DE INGRESOS
    // ==========================================
    public List<IngresoStock> listarTodos() {
        // Retorna todas las cabeceras de compras a proveedores
        return ingresoStockRepository.findAll();
    }

    // ==========================================
    // 1.5 LISTAR HISTORIAL PAGINADO
    // ==========================================
    public Page<IngresoStock> listarHistorialPaginado(Pageable pageable) {
        return ingresoStockRepository.findAll(pageable);
    }

    // ==========================================
    // 2. REGISTRAR INGRESO Y SUMAR STOCK
    // ==========================================
    @Transactional
    public IngresoStock registrarIngresoMercaderia(IngresoStock ingresoStock, Usuario cajero) {

        // 1. Validar que la cabecera tenga fecha, si no, le ponemos la de hoy
        if (ingresoStock.getFechaIngreso() == null) {
            ingresoStock.setFechaIngreso(LocalDateTime.now());
        }

        if (ingresoStock.getDetallesIngreso() == null || ingresoStock.getDetallesIngreso().isEmpty()) {
            throw new IllegalArgumentException("No puedes registrar un ingreso vacío. Agrega al menos un producto.");
        }

        // FASE 7 — BARRERA ANTI-NEGATIVOS:
        // Si la compra se paga en efectivo, validamos el saldo de caja ANTES
        // de tocar cualquier stock o guardar cualquier registro. Si no
        // alcanza, la operación se detiene aquí completa: no se descuenta
        // stock a medias, no se guarda ninguna cabecera huérfana.
        BigDecimal costoTotalLote = ingresoStock.getDetallesIngreso().stream()
                .map(d -> d.getPrecioCompra().multiply(d.getCantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if ("Efectivo".equalsIgnoreCase(ingresoStock.getMetodoPago())) {
            cajaService.validarSaldoSuficienteParaCompra(costoTotalLote);
        }

        for (DetalleIngresoStock detalle : ingresoStock.getDetallesIngreso()) {

            // Regla de Negocio: Máximo 99 unidades/kg por lote
            if (detalle.getCantidad().compareTo(new BigDecimal("99.00")) > 0) {
                throw new IllegalArgumentException("Bloqueo de seguridad: No puedes ingresar un lote mayor a 99 unidades de golpe para un mismo producto.");
            }

            // Buscamos el producto físico en la base de datos
            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("El producto seleccionado no existe en el catálogo."));

            // Lógica Pulida: Soporta ingresos con fracciones (Ej: Compras 1.5 Cajas)
            if (producto.getPermiteFraccionamiento() != null && producto.getPermiteFraccionamiento()) {

                // Extraemos la parte entera y la decimal de forma matemática segura
                BigDecimal cantidadEnteraBD = detalle.getCantidad().setScale(0, RoundingMode.DOWN);
                int enteros = cantidadEnteraBD.intValue();
                BigDecimal decimales = detalle.getCantidad().subtract(cantidadEnteraBD);

                int stockCerradoActual = producto.getStockCerrado() != null ? producto.getStockCerrado() : 0;
                producto.setStockCerrado(stockCerradoActual + enteros); // Suma las cajas enteras

                if (decimales.compareTo(BigDecimal.ZERO) > 0) {
                    // Convertimos la fracción (ej: 0.5 cajas) a sueltos (ej: 0.5 * 10 = 5 blisters)
                    BigDecimal contenidoExtra = decimales.multiply(producto.getContenidoPorEnvase() != null ? producto.getContenidoPorEnvase() : BigDecimal.ZERO);
                    producto.setStockAbierto(producto.getStockAbierto().add(contenidoExtra));
                }
            } else {
                // Para productos no fraccionables, la cantidad debe ser entera.
                int cantidadEntera = detalle.getCantidad().intValue();
                if (detalle.getCantidad().compareTo(new BigDecimal(cantidadEntera)) != 0) {
                    throw new IllegalArgumentException("La cantidad de ingreso para '" + producto.getNombre() + "' debe ser un número entero cerrado.");
                }
                // El stock se suma a 'stockCerrado' que ahora representa las unidades.
                int stockCerradoActual = producto.getStockCerrado() != null ? producto.getStockCerrado() : 0;
                producto.setStockCerrado(stockCerradoActual + cantidadEntera);
            }

            // Guardamos el producto con su nuevo stock
            productoRepository.save(producto);

            // Amarramos el detalle a la cabecera para que MySQL entienda la relación (Llave Foránea)
            detalle.setIngresoStock(ingresoStock);
        }

        // 3. Guardamos la cabecera (Hibernate guardará los detalles en cascada automáticamente)
        IngresoStock guardado = ingresoStockRepository.save(ingresoStock);

        // 4. Recién aquí, con el ingreso ya persistido (tiene id), descontamos
        // el saldo de caja si correspondía pagarlo en efectivo.
        if ("Efectivo".equalsIgnoreCase(guardado.getMetodoPago())) {
            cajaService.registrarEgresoCompraEfectivo(costoTotalLote, guardado, cajero);
        }
        return guardado;
    }
}