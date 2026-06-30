package com.Veterinaria.Mejia.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Veterinaria.Mejia.dto.CatalogoProductoDTO;
import com.Veterinaria.Mejia.dto.CatalogoServicioDTO;
import com.Veterinaria.Mejia.models.Cliente;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.models.Servicio;
import com.Veterinaria.Mejia.repository.ClienteRepository;
import com.Veterinaria.Mejia.repository.DuenoRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;
import com.Veterinaria.Mejia.repository.ServicioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST encargado de proveer datos utilitarios en formato JSON
 * para la gestión y cálculo de comprobantes de pago.
 */
@RestController
@RequestMapping("/api/utilidades")
@RequiredArgsConstructor
public class ComprobanteApiController {

    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;
    private final ClienteRepository clienteRepository;
    private final DuenoRepository duenoRepository;

    /**
     * Obtiene de forma asíncrona el precio de venta y el stock actual de un producto.
     * Útil para actualizar dinámicamente los totales en la interfaz al agregar un ítem.
     *
     * @param id Identificador único del producto (Integer).
     * @return ResponseEntity con un Map que contiene el estado de la consulta, precio y stock.
     * @route GET /api/utilidades/producto/{id}
     */
    @GetMapping("/producto/{id}")
    @SuppressWarnings("null")
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
    @SuppressWarnings("null")
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
     * Primero busca coincidencia entre los Dueños registrados (dueños de mascotas),
     * y si no se encuentra, se busca entre los Clientes ya registrados directamente.
     *
     * @param dni Documento a buscar.
     * @return ResponseEntity con el nombre, teléfono y dirección del cliente/dueño si fue encontrado.
     */
    @GetMapping("/cliente/{dni}")
    public ResponseEntity<Map<String, Object>> obtenerInfoCliente(@PathVariable String dni) {
        Map<String, Object> respuesta = new HashMap<>();

        // 1. Buscar primero entre los Dueños registrados (dueños de mascotas/pacientes)
        var duenoOpt = duenoRepository.findByDni(dni);
        if (duenoOpt.isPresent()) {
            var dueno = duenoOpt.get();
            respuesta.put("success", true);
            respuesta.put("origen", "DUENO");
            respuesta.put("nombre", dueno.getNombre());
            respuesta.put("telefono", dueno.getTelefono());
            respuesta.put("direccion", dueno.getDireccion());
            return ResponseEntity.ok(respuesta);
        }

        // 2. Si no es un dueño, buscar entre los Clientes ya registrados directamente
        Cliente cliente = clienteRepository.findByNumeroDocumento(dni).orElse(null);
        if (cliente != null) {
            respuesta.put("success", true);
            respuesta.put("origen", "CLIENTE");
            respuesta.put("nombre", cliente.getNombre());
            respuesta.put("telefono", cliente.getTelefono());
            respuesta.put("direccion", cliente.getDireccion());
        } else {
            // 3. No se encontró ningún registro: se permite igualmente registrar la venta
            // a nombre del DNI ingresado (el cajero puede escribir el nombre manualmente).
            respuesta.put("success", false);
        }

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Busca un dueño por su ID y devuelve sus datos de contacto.
     * Usado para autocompletar formularios.
     *
     * @param id ID del dueño.
     * @return ResponseEntity con los datos del dueño.
     */
    @GetMapping("/dueno/{id}")
    public ResponseEntity<Map<String, Object>> obtenerInfoDueno(@PathVariable Integer id) {
        Map<String, Object> respuesta = new HashMap<>();
        duenoRepository.findById(id).ifPresentOrElse(dueno -> {
            respuesta.put("success", true);
            respuesta.put("nombre", dueno.getNombre());
            respuesta.put("dni", dueno.getDni());
            respuesta.put("telefono", dueno.getTelefono());
            respuesta.put("email", dueno.getEmail());
            respuesta.put("direccion", dueno.getDireccion());
        }, () -> {
            respuesta.put("success", false);
        });
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Busca productos para el catálogo del POS con filtros opcionales.
     * Excluye productos de uso clínico exclusivo (no vendibles directamente al público).
     *
     * @param q           Término de búsqueda para el nombre del producto.
     * @param categoriaId ID de la categoría para filtrar.
     * @param especieId   ID de la especie para filtrar.
     * @return Lista de productos que coinciden con los filtros.
     */
    @GetMapping("/productos/buscar")
    public ResponseEntity<List<CatalogoProductoDTO>> buscarProductosCatalogo(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer especieId) {
        List<CatalogoProductoDTO> productos = productoRepository.buscarYFiltrarInventarioJPQL(q, categoriaId, especieId)
                .stream()
                .filter(Producto::isEstado)
                .map(CatalogoProductoDTO::desde)
                .toList();
        return ResponseEntity.ok(productos);
    }

    /**
     * Catálogo combinado del Punto de Venta: trae productos (filtrados por nombre,
     * categoría y/o especie) junto con la lista completa de servicios clínicos activos.
     * Pensado para alimentar la tabla del modal "Catálogo de Inventario y Servicios".
     *
     * @param q           Término de búsqueda (aplica a productos y servicios).
     * @param categoriaId ID de la categoría para filtrar productos.
     * @param especieId   ID de la especie para filtrar productos.
     * @return Mapa con dos listas: "productos" y "servicios".
     */
    @GetMapping("/catalogo-pos")
    public ResponseEntity<Map<String, Object>> catalogoPOS(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer especieId) {

        List<CatalogoProductoDTO> productos = productoRepository.buscarYFiltrarInventarioJPQL(q, categoriaId, especieId)
                .stream()
                .filter(Producto::isEstado)
                .map(CatalogoProductoDTO::desde)
                .toList();

        List<Servicio> serviciosBase = servicioRepository.findByEstadoTrue();
        if (q != null && !q.isBlank()) {
            String qLower = q.toLowerCase();
            serviciosBase = serviciosBase.stream()
                    .filter(s -> s.getNombreServicio().toLowerCase().contains(qLower))
                    .toList();
        }
        // Los servicios solo aplican cuando NO se filtra por especie/categoría específica de producto,
        // ya que son de carácter general (no pertenecen a una especie/categoría de inventario).
        List<CatalogoServicioDTO> servicios = (categoriaId != null || especieId != null)
                ? List.of()
                : serviciosBase.stream().map(CatalogoServicioDTO::desde).toList();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("productos", productos);
        respuesta.put("servicios", servicios);
        return ResponseEntity.ok(respuesta);
    }
}