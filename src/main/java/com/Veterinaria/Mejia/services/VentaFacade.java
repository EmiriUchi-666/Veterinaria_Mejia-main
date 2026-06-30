package com.Veterinaria.Mejia.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.dto.ItemCarritoDTO;
import com.Veterinaria.Mejia.dto.VentaRequestDTO;
import com.Veterinaria.Mejia.models.FacturacionEstado;
import com.Veterinaria.Mejia.models.Venta;
import com.Veterinaria.Mejia.repository.FacturacionEstadoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VentaFacade {
    private static final Logger log = LoggerFactory.getLogger(VentaFacade.class);

    private final VentaConsultaService ventaConsultaService;
    private final VentaCreacionService ventaCreacionService;
    private final VentaAnulacionService ventaAnulacionService;
    private final FacturacionService facturacionService;
    private final FacturacionEstadoRepository facturacionEstadoRepository;

    @Transactional(readOnly = true)
    public List<Venta> listarUltimas10VentasConDetalles() {
        return ventaConsultaService.listarUltimas10VentasConDetalles();
    }

    public Venta buscarPorId(Integer id) {
        return ventaConsultaService.buscarPorId(id);
    }

    public Venta procesarVentaTransaccional(VentaRequestDTO request, Authentication authentication) {
        // Paso 1: Crear la venta en la base de datos
        Venta ventaGuardada = ventaCreacionService.procesarVentaTransaccional(request, authentication);

        // Paso 2: Registrar el intento de facturación en estado PENDIENTE
        FacturacionEstado estadoFacturacion = FacturacionEstado.builder()
                .venta(ventaGuardada)
                .estado(FacturacionEstado.EstadoFacturacion.PENDIENTE)
                .build();
        facturacionEstadoRepository.save(estadoFacturacion);

        // Paso 3: Delegar el envío y la actualización de estado al servicio especializado
        facturacionService.procesarEnvioNubefact(estadoFacturacion);
        
        return ventaGuardada;
    }
    
    /**
     * Nuevo método que encapsula la creación del DTO.
     * El controlador ahora solo pasa los parámetros del formulario.
     */
    public Venta procesarVentaDesdeFormulario(
            Venta venta, String dni, String nombre, String telefono, String direccion,
            List<String> tipos, List<Integer> ids, List<String> cantidades,
            List<String> precios,
            Authentication authentication) {
            
        if (tipos == null || tipos.isEmpty()) {
            throw new IllegalArgumentException("El carrito no puede estar vacío.");
        }

        VentaRequestDTO request = new VentaRequestDTO();
        request.setClienteNumDoc(dni);
        
        // --- DETECCIÓN AUTOMÁTICA DE DOCUMENTO ---
        // Si tiene 11 dígitos exactos asumimos RUC, de lo contrario DNI.
        if (dni != null && dni.trim().length() == 11) {
            request.setClienteTipoDoc("RUC");
        } else {
            request.setClienteTipoDoc("DNI");
        }
        
        request.setClienteNombre(nombre);
        request.setClienteTelefono(telefono);
        request.setClienteDireccion(direccion);
        request.setTipoPago(venta.getTipoPago());
        request.setTotal(venta.getTotalVenta());

        List<ItemCarritoDTO> items = new ArrayList<>();
        for (int i = 0; i < tipos.size(); i++) {
            ItemCarritoDTO item = new ItemCarritoDTO();
            item.setTipo(tipos.get(i));
            item.setIdItem(ids.get(i));
            item.setCantidad(new BigDecimal(cantidades.get(i)));
            item.setPrecio(new BigDecimal(precios.get(i)));
            // El subtotal se recalcula en el backend por seguridad
            item.setSubtotal(item.getPrecio().multiply(item.getCantidad()));
            items.add(item);
        }
        request.setItems(items);
        
        return this.procesarVentaTransaccional(request, authentication);
    }

    public void anularVenta(Integer id) {
        ventaAnulacionService.anularVenta(id);
    }
}