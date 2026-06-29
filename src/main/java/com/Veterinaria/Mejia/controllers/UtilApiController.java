package com.Veterinaria.Mejia.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Veterinaria.Mejia.services.ContingenciaService;
import com.Veterinaria.Mejia.services.SunatValidacionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UtilApiController {

    private final SunatValidacionService validacionSvc;
    private final ContingenciaService contingenciaSvc;

    /** GET /api/validacion/ruc/{ruc} — Valida RUC peruano */
    @GetMapping("/validacion/ruc/{ruc}")
    public ResponseEntity<?> validarRuc(@PathVariable String ruc) {
        var r = validacionSvc.validarRUC(ruc);
        return ResponseEntity.ok(Map.of("valido", r.valido(), "mensaje", r.mensaje(), "ruc", ruc));
    }

    /** GET /api/validacion/dni/{dni} — Valida DNI peruano */
    @GetMapping("/validacion/dni/{dni}")
    public ResponseEntity<?> validarDni(@PathVariable String dni) {
        var r = validacionSvc.validarDNI(dni);
        return ResponseEntity.ok(Map.of("valido", r.valido(), "mensaje", r.mensaje(), "dni", dni));
    }

    /** GET /api/contingencia/estado — Estado de la cola SUNAT */
    @GetMapping("/contingencia/estado")
    public ResponseEntity<?> estadoContingencia() {
        return ResponseEntity.ok(Map.of(
            "pendientes", contingenciaSvc.contarPendientes(),
            "cola", contingenciaSvc.listarCola()
        ));
    }
}
