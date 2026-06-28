package com.Veterinaria.Mejia.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura las excepciones de validación y reglas de negocio (Ej: Carrito vacío, Clave inválida)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        
        // Retorna un HTTP 400 (Bad Request) con el JSON
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Captura excepciones de ejecución (Ej: Producto no encontrado, Stock insuficiente)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        // Puedes usar CONFLICT (409) o INTERNAL_SERVER_ERROR (500) según prefieras
        response.put("status", HttpStatus.CONFLICT.value()); 
        
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}