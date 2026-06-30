package com.Veterinaria.Mejia.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Veterinaria.Mejia.models.Cliente;
import com.Veterinaria.Mejia.repository.ClienteRepository;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints REST de Cliente. Pensado para autocompletar formularios
 * (alta rápida de dueño/tutor, panel de venta) cuando el usuario selecciona
 * un cliente ya existente de un &lt;select&gt;.
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteApiController {

    private final ClienteRepository clienteRepository;

    /**
     * Devuelve los datos de contacto de un cliente por su ID.
     * Usado por JavaScript (fetch) para autocompletar nombre/DNI/teléfono
     * en campos de solo lectura cuando se elige un cliente existente.
     *
     * @param id ID del cliente.
     * @return Mapa JSON con success + datos del cliente, o success=false si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerClientePorId(@PathVariable Integer id) {
        Map<String, Object> respuesta = new HashMap<>();

        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) {
            respuesta.put("success", false);
            respuesta.put("message", "Cliente no encontrado.");
            return ResponseEntity.ok(respuesta);
        }

        respuesta.put("success", true);
        respuesta.put("id", cliente.getId());
        respuesta.put("nombre", cliente.getNombre());
        respuesta.put("numeroDocumento", cliente.getNumeroDocumento());
        respuesta.put("tipoDocumento", cliente.getTipoDocumento());
        respuesta.put("telefono", cliente.getTelefono());
        respuesta.put("email", cliente.getEmail());
        respuesta.put("direccion", cliente.getDireccion());
        return ResponseEntity.ok(respuesta);
    }
}
