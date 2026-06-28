package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.dto.ItemCarritoDTO;
import com.Veterinaria.Mejia.dto.VentaRequestDTO;
import com.Veterinaria.Mejia.models.*;
import com.Veterinaria.Mejia.repository.*;

@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    @Autowired private VentaRepository ventaRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ServicioRepository servicioRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Transactional(rollbackFor = Exception.class)
    public Venta procesarVentaTransaccional(VentaRequestDTO request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("La venta no puede procesarse porque el carrito está vacío.");
        }

        Venta venta = new Venta();
        venta.setFechaEmision(LocalDateTime.now());
        venta.setTotalVenta(request.getTotal());
        venta.setTipoPago(request.getTipoPago());
        venta.setEstado(true);
        venta.setDetallesVentas(new ArrayList<>());

        // ── C1 FIX: Usuario desde Spring Security ──────────────────────────
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new SecurityException("Sesión no válida. Por favor inicie sesión para registrar ventas.");
        }
        Usuario cajero = usuarioRepository.findByNombreUsuario(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en BD: " + auth.getName()));
        venta.setUsuario(cajero);
        log.info("[VENTA] Procesando venta por usuario={} | items={} | total={}",
                cajero.getNombreUsuario(), request.getItems().size(), request.getTotal());

        // ── Cliente ─────────────────────────────────────────────────────────
        String dniIngresado = request.getClienteDni();
        String nombreIngresado = request.getClienteNombre();
        if (dniIngresado != null && !dniIngresado.trim().isEmpty()) {
            Optional<Cliente> clienteExistente = clienteRepository.findByDniJPQL(dniIngresado);
            if (clienteExistente.isPresent()) {
                venta.setCliente(clienteExistente.get());
            } else {
                Cliente nuevoCliente = new Cliente();
                nuevoCliente.setDni(dniIngresado);
                nuevoCliente.setNombre(nombreIngresado != null && !nombreIngresado.trim().isEmpty()
                        ? nombreIngresado : "Cliente sin nombre");
                venta.setCliente(clienteRepository.save(nuevoCliente));
            }
        } else {
            venta.setCliente(null); // Consumidor final
        }

        // ── Tipo comprobante por monto ───────────────────────────────────────
        if (venta.getTotalVenta().compareTo(new BigDecimal("5.00")) >= 0) {
            venta.setTipoComprobante("Boleta");
            venta.setSerie("B001");
        } else {
            venta.setTipoComprobante("Ticket");
            venta.setSerie("T001");
        }

        // ── Correlativo ─────────────────────────────────────────────────────
        Integer ultimoCorrelativo = ventaRepository.obtenerMaximoCorrelativoJPQL(venta.getSerie());
        venta.setCorrelativo(ultimoCorrelativo == null ? 1 : ultimoCorrelativo + 1);

        // ── Items del carrito ────────────────────────────────────────────────
        Set<Integer> serviciosCobrados = new HashSet<>();
        for (ItemCarritoDTO item : request.getItems()) {
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setCantidad(item.getCantidad());

            if ("SERVICIO".equalsIgnoreCase(item.getTipo())) {
                Servicio serv = servicioRepository.findById(item.getIdItem())
                        .orElseThrow(() -> new RuntimeException("Servicio no encontrado."));
                if (!serviciosCobrados.add(serv.getId())) {
                    throw new IllegalArgumentException("No puedes registrar el mismo servicio más de una vez.");
                }
                detalle.setServicio(serv);
                detalle.setPrecioUnitario(serv.getPrecioServicio());
                // A2 FIX: BigDecimal con HALF_UP
                detalle.setSubtotal(serv.getPrecioServicio()
                        .multiply(item.getCantidad()).setScale(2, RoundingMode.HALF_UP));

            } else if ("PRODUCTO_ENTERO".equalsIgnoreCase(item.getTipo())
                    || "PRODUCTO_FRACCIONADO".equalsIgnoreCase(item.getTipo())) {

                // A6 FIX: Optimistic locking via refresh + version check
                Producto prod = productoRepository.findByIdWithLock(item.getIdItem())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getIdItem()));

                boolean esFraccionado = "PRODUCTO_FRACCIONADO".equalsIgnoreCase(item.getTipo());

                if (esFraccionado) {
                    if (!Boolean.TRUE.equals(prod.getPermiteFraccionamiento())) {
                        throw new IllegalArgumentException("El producto '" + prod.getNombre() + "' no permite venta fraccionada.");
                    }
                    BigDecimal cantReq = item.getCantidad();
                    while (prod.getStockAbierto().compareTo(cantReq) < 0) {
                        if (prod.getStockCerrado() == null || prod.getStockCerrado() <= 0) {
                            throw new RuntimeException("Stock insuficiente para venta suelta de: " + prod.getNombre());
                        }
                        prod.setStockCerrado(prod.getStockCerrado() - 1);
                        prod.setStockAbierto(prod.getStockAbierto().add(prod.getContenidoPorEnvase()));
                    }
                    prod.setStockAbierto(prod.getStockAbierto().subtract(cantReq));
                    detalle.setPrecioUnitario(prod.getPrecioPorFraccion());
                    detalle.setSubtotal(prod.getPrecioPorFraccion()
                            .multiply(item.getCantidad()).setScale(2, RoundingMode.HALF_UP));
                } else {
                    if (prod.getStockTotal().compareTo(item.getCantidad()) < 0) {
                        throw new RuntimeException("Stock insuficiente de '" + prod.getNombre()
                                + "'. Disponible: " + prod.getStockTotal());
                    }
                    prod.setStockTotal(prod.getStockTotal().subtract(item.getCantidad()));
                    detalle.setPrecioUnitario(prod.getPrecioVentaActual());
                    detalle.setSubtotal(prod.getPrecioVentaActual()
                            .multiply(item.getCantidad()).setScale(2, RoundingMode.HALF_UP));
                }
                detalle.setProducto(prod);
                productoRepository.save(prod);

            } else {
                throw new IllegalArgumentException("Tipo de ítem no reconocido: " + item.getTipo());
            }
            venta.getDetallesVentas().add(detalle);
        }

        Venta guardada = ventaRepository.save(venta);
        log.info("[VENTA] Venta #{} registrada por usuario={} | total=S/ {}",
                guardada.getId(), cajero.getNombreUsuario(), guardada.getTotalVenta());
        return guardada;
    }
}
