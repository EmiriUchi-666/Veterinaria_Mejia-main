package com.Veterinaria.Mejia.controllers;

import com.Veterinaria.Mejia.services.ContingenciaService;
import com.Veterinaria.Mejia.services.SunatValidacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UtilApiController {

    @Autowired private SunatValidacionService validacionSvc;
    @Autowired private ContingenciaService contingenciaSvc;

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
