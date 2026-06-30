package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final CajaService cajaService;

    // FIX: antes estaban hardcodeadas "B001"/"F001" en el código. En modo
    // sandbox (nubefact.modo.prueba=true), Nubefact SOLO acepta sus series
    // de demostración, no series reales — por eso fallaba con "No puedes
    // emitir comprobantes con esta serie". Ahora son configurables.
    @Value("${nubefact.serie.boleta:BBB1}")
    private String serieBoleta;

    @Value("${nubefact.serie.factura:FFF1}")
    private String serieFactura;

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
        String telefonoIngresado = request.getClienteTelefono();
        String direccionIngresado = request.getClienteDireccion();

        // Validación de negocio (SUNAT): a partir de S/ 700.00 el comprobante exige
        // identificación del cliente (DNI/RUC). Se valida también en el servidor
        // como defensa adicional a la validación que ya existe en el formulario.
        BigDecimal umbralIdentificacionObligatoria = new BigDecimal("700.00");
        boolean requiereIdentificacion = request.getTotal() != null
                && request.getTotal().compareTo(umbralIdentificacionObligatoria) >= 0;
        boolean tieneDocumentoValido = numDocIngresado != null && numDocIngresado.trim().length() >= 8;
        if (requiereIdentificacion && !tieneDocumentoValido) {
            throw new IllegalArgumentException(
                    "Por exigencia de SUNAT, las boletas o facturas a partir de S/ 700.00 requieren obligatoriamente el DNI/RUC del cliente.");
        }

        if (numDocIngresado != null && !numDocIngresado.trim().isEmpty()) {
            // 1. SIEMPRE buscamos o creamos el Cliente primero. Esto evita el Duplicate Entry.
            String nombreFinal = (nombreIngresado != null && !nombreIngresado.isBlank())
                    ? nombreIngresado.trim()
                    : "Cliente DNI " + numDocIngresado;

            Cliente clienteParaVenta = clienteRepository.findByNumeroDocumento(numDocIngresado)
                    .orElseGet(() -> clienteRepository.save(Cliente.builder()
                            .numeroDocumento(numDocIngresado)
                            .tipoDocumento(tipoDocIngresado != null ? tipoDocIngresado : "DNI")
                            .nombre(nombreFinal)
                            .telefono(telefonoIngresado != null && !telefonoIngresado.isBlank() ? telefonoIngresado.trim() : null)
                            .direccion(direccionIngresado != null && !direccionIngresado.isBlank() ? direccionIngresado.trim() : null)
                            .build()));

            // 2. Revisamos si este DNI también pertenece a un Dueño (Paciente)
            Optional<Dueno> duenoExistente = duenoRepository.findByDni(numDocIngresado);
            if (duenoExistente.isPresent()) {
                Dueno dueno = duenoExistente.get();
                // Si el dueño existe pero su perfil no está enlazado a este cliente, los conectamos
                if (dueno.getCliente() == null) {
                    dueno.setCliente(clienteParaVenta);
                    duenoRepository.save(dueno);
                }
            }

            // 3. Asignamos el cliente (existente o recién creado) a la venta
            venta.setCliente(clienteParaVenta);
            
        } else {
            // Lógica intacta para Público General (Boletas menores a 700)
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
            venta.setSerie(serieFactura);
        } else {
            venta.setTipoComprobante("Boleta");
            venta.setSerie(serieBoleta);
        }

        Integer ultimoCorrelativo = ventaRepository.obtenerMaximoCorrelativoJPQL(venta.getSerie());
        venta.setCorrelativo(ultimoCorrelativo == null ? 1 : ultimoCorrelativo + 1);
        // -----------------------------------------------

        procesarItems(request, venta);

        Venta guardada = ventaRepository.save(venta);
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