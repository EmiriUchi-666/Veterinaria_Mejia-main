package com.Veterinaria.Mejia.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Veterinaria.Mejia.models.Cliente;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.models.Servicio;
import com.Veterinaria.Mejia.repository.ClienteRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;
import com.Veterinaria.Mejia.repository.ServicioRepository;

/**
 * Controlador REST encargado de proveer datos utilitarios en formato JSON
 * para la gestión y cálculo de comprobantes de pago.
 */
@RestController
@RequestMapping("/api/utilidades")
public class ComprobanteApiController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Obtiene de forma asíncrona el precio de venta y el stock actual de un producto.
     * Útil para actualizar dinámicamente los totales en la interfaz al agregar un ítem.
     *
     * @param id Identificador único del producto (Integer).
     * @return ResponseEntity con un Map que contiene el estado de la consulta, precio y stock.
     * @route GET /api/utilidades/producto/{id}
     */
    @GetMapping("/producto/{id}")
    public ResponseEntity<Map<String, Object>> obtenerInfoProducto(@PathVariable Integer id) {
        Map<String, Object> respuesta = new HashMap<>();
        
        // Búsqueda en base de datos mediante JPA; retorna null si el ID no existe
        Producto prod = productoRepository.findById(id).orElse(null);
        
        if (prod != null) {
            respuesta.put("success", true);
            respuesta.put("precio", prod.getPrecioVentaActual());
            respuesta.put("stock", prod.getStockTotal());
        } else {
            respuesta.put("success", false);
            // Nota para el equipo: Podríamos agregar un campo "message" detallando el error si es necesario
        }
        
        // Retorna HTTP 200 OK con el mapa serializado en JSON
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene el precio base de un servicio específico.
     * Al igual que con los productos, asiste en la carga de montos en el detalle del comprobante.
     *
     * @param id Identificador único del servicio (Integer).
     * @return ResponseEntity con un Map que contiene el estado de la consulta y el precio del servicio.
     * @route GET /api/utilidades/servicio/{id}
     */
    @GetMapping("/servicio/{id}")
    public ResponseEntity<Map<String, Object>> obtenerInfoServicio(@PathVariable Integer id) {
        Map<String, Object> respuesta = new HashMap<>();
        
        // Búsqueda en base de datos mediante el repositorio de servicios
        Servicio serv = servicioRepository.findById(id).orElse(null);
        
        if (serv != null) {
            respuesta.put("success", true);
            respuesta.put("precio", serv.getPrecioServicio());
        } else {
            respuesta.put("success", false);
        }
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Busca un cliente por su documento de identidad (DNI/RUC).
     *
     * @param dni Documento a buscar.
     * @return ResponseEntity con el nombre del cliente si fue encontrado.
     */
    @GetMapping("/cliente/{dni}")
    public ResponseEntity<Map<String, Object>> obtenerInfoCliente(@PathVariable String dni) {
        Map<String, Object> respuesta = new HashMap<>();
        
        // Utilizamos el método ya existente en tu ClienteRepository
        Cliente cliente = clienteRepository.findByDniJPQL(dni).orElse(null);
        
        if (cliente != null) {
            respuesta.put("success", true);
            respuesta.put("nombre", cliente.getNombre());
        } else {
            respuesta.put("success", false);
        }
        
        return ResponseEntity.ok(respuesta);
    }
}