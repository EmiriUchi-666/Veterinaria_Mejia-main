package com.Veterinaria.Mejia.services;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.dto.ItemCarritoDTO;
import com.Veterinaria.Mejia.dto.VentaRequestDTO;
import com.Veterinaria.Mejia.models.Cliente;
import com.Veterinaria.Mejia.models.DetalleVenta;
import com.Veterinaria.Mejia.models.Dueno;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.models.Servicio;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.repository.ClienteRepository;
import com.Veterinaria.Mejia.repository.DuenoRepository;
import com.Veterinaria.Mejia.repository.ServicioRepository;
import com.Veterinaria.Mejia.repository.UsuarioRepository;
import com.Veterinaria.Mejia.repository.VentaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VentaCreacionService {

    private static final Logger log = LoggerFactory.getLogger(VentaCreacionService.class);

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final DuenoRepository duenoRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioService inventarioService;

    @Transactional(rollbackFor = Exception.class)
    public Venta procesarVentaTransaccional(VentaRequestDTO request, Authentication authentication) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("La venta no puede procesarse porque el carrito está vacío.");
        }

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new SecurityException("Sesión no válida. Por favor inicie sesión para registrar ventas.");
        }
        Usuario cajero = usuarioRepository.findByNombreUsuario(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en BD: " + authentication.getName()));

        Venta venta = new Venta();
        venta.setFechaEmision(LocalDateTime.now());
        venta.setTotalVenta(request.getTotal());
        venta.setTipoPago(request.getTipoPago());
        venta.setEstado(true);
        venta.setDetallesVentas(new ArrayList<>());
        venta.setUsuario(cajero);
        log.info("[VENTA] Procesando venta por usuario={} | items={} | total={}",
                cajero.getNombreUsuario(), request.getItems().size(), request.getTotal());

        String numDocIngresado = request.getClienteNumDoc();
        String tipoDocIngresado = request.getClienteTipoDoc();
        String nombreIngresado = request.getClienteNombre();

        if (numDocIngresado != null && !numDocIngresado.trim().isEmpty()) {
            // Buscar primero en Dueños
            Optional<Dueno> duenoExistente = duenoRepository.findByDni(numDocIngresado);
            Cliente clienteParaVenta;
            if (duenoExistente.isPresent()) {
                Dueno dueno = duenoExistente.get();
                // Usar el cliente asociado al dueño o crearlo si no existe
                clienteParaVenta = Optional.ofNullable(dueno.getCliente())
                    .orElseGet(() -> {
                        Cliente nuevoClienteDesdeDueno = new Cliente();
                        nuevoClienteDesdeDueno.setNombre(dueno.getNombre());
                        nuevoClienteDesdeDueno.setNumeroDocumento(dueno.getDni());
                        nuevoClienteDesdeDueno.setTipoDocumento(dueno.getTipoDocumento());
                        nuevoClienteDesdeDueno.setTelefono(dueno.getTelefono());
                        nuevoClienteDesdeDueno.setEmail(dueno.getEmail());
                        nuevoClienteDesdeDueno.setDireccion(dueno.getDireccion());
                        dueno.setCliente(nuevoClienteDesdeDueno); // Asocia el nuevo cliente al dueño
                        duenoRepository.save(dueno); // Guarda el dueño con la nueva asociación
                        return nuevoClienteDesdeDueno;
                    });
            } else {
                // Si no es un dueño, buscar o crear en Clientes
                clienteParaVenta = clienteRepository.findByNumeroDocumento(numDocIngresado)
                    .orElseGet(() -> clienteRepository.save(Cliente.builder()
                            .numeroDocumento(numDocIngresado)
                            .tipoDocumento(tipoDocIngresado)
                            .nombre(nombreIngresado != null && !nombreIngresado.isBlank() ? nombreIngresado : "Cliente sin nombre")
                            .build()));
            }
            venta.setCliente(clienteParaVenta);
        } else {
            Cliente publicoGeneral = clienteRepository.findByNumeroDocumento("00000000")
                    .orElseGet(() -> clienteRepository.save(Cliente.builder()
                            .nombre("PÚBLICO GENERAL")
                            .tipoDocumento("DNI")
                            .numeroDocumento("00000000")
                            .build()));
            venta.setCliente(publicoGeneral);
        }

        if (venta.getCliente() != null && "RUC".equals(venta.getCliente().getTipoDocumento())) {
            venta.setTipoComprobante("Factura");
            venta.setSerie("F001");
        } else {
            venta.setTipoComprobante("Boleta");
            venta.setSerie("B001");
        }

        Integer ultimoCorrelativo = ventaRepository.obtenerMaximoCorrelativoJPQL(venta.getSerie());
        venta.setCorrelativo(ultimoCorrelativo == null ? 1 : ultimoCorrelativo + 1);

        procesarItems(request, venta);

        Venta guardada = ventaRepository.save(venta);
        log.info("[VENTA] Venta #{} registrada por usuario={} | total=S/ {}",
                guardada.getId(), cajero.getNombreUsuario(), guardada.getTotalVenta());
        return guardada;
    }

    private void procesarItems(VentaRequestDTO request, Venta venta) {
        HashSet<Integer> serviciosCobrados = new HashSet<>();
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
                detalle.setSubtotal(serv.getPrecioServicio().multiply(item.getCantidad()).setScale(2, RoundingMode.HALF_UP));
            } else if ("PRODUCTO_ENTERO".equalsIgnoreCase(item.getTipo()) || "PRODUCTO_FRACCIONADO".equalsIgnoreCase(item.getTipo())) {
                // Delegamos la lógica de descuento de stock al servicio especializado
                Producto prod = inventarioService.descontarStockDeProducto(item);
                detalle.setProducto(prod);
                
                // Asignamos el precio y subtotal correspondiente al tipo de venta
                if ("PRODUCTO_FRACCIONADO".equalsIgnoreCase(item.getTipo())) {
                    detalle.setPrecioUnitario(prod.getPrecioPorFraccion());
                    detalle.setSubtotal(prod.getPrecioPorFraccion()
                            .multiply(item.getCantidad()).setScale(2, RoundingMode.HALF_UP));
                } else {
                    detalle.setPrecioUnitario(prod.getPrecioVentaActual());
                    detalle.setSubtotal(prod.getPrecioVentaActual()
                            .multiply(item.getCantidad()).setScale(2, RoundingMode.HALF_UP));
                }

            } else {
                throw new IllegalArgumentException("Tipo de ítem no reconocido: " + item.getTipo());
            }
            venta.getDetallesVentas().add(detalle);
        }
    }
}