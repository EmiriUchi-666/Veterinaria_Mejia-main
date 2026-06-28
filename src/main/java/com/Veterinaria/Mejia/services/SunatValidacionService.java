package com.Veterinaria.Mejia.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * A1 — Validación de RUC y DNI peruana.
 * Implementa el algoritmo oficial del dígito verificador SUNAT para RUC de 11 dígitos.
 * No requiere API externa; el cálculo es determinístico.
 */
@Service
public class SunatValidacionService {

    private static final Logger log = LoggerFactory.getLogger(SunatValidacionService.class);

    /** Factores de ponderación SUNAT para dígito verificador de RUC */
    private static final int[] FACTORES_RUC = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    /**
     * Valida un número de RUC peruano (11 dígitos).
     * Algoritmo oficial SUNAT — sin necesidad de API externa.
     *
     * @param ruc String con el RUC (puede incluir espacios o guiones)
     * @return true si el RUC es matemáticamente válido
     */
    @Cacheable("rucValidados")
    public ResultadoValidacion validarRUC(String ruc) {
        if (ruc == null) return new ResultadoValidacion(false, "RUC nulo.");
        String r = ruc.trim().replaceAll("[^0-9]", "");

        if (r.length() != 11) {
            return new ResultadoValidacion(false, "El RUC debe tener exactamente 11 dígitos. Recibido: " + r.length());
        }
        // Prefijos válidos: 10 (persona natural), 20 (empresa), 15, 17
        String prefijo = r.substring(0, 2);
        if (!prefijo.equals("10") && !prefijo.equals("20") &&
                !prefijo.equals("15") && !prefijo.equals("17")) {
            return new ResultadoValidacion(false, "Prefijo de RUC inválido: " + prefijo + ". Debe comenzar con 10, 15, 17 o 20.");
        }

        // Cálculo dígito verificador
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += Character.getNumericValue(r.charAt(i)) * FACTORES_RUC[i];
        }
        int resto = suma % 11;
        int digitoCalculado = 11 - resto;
        if (digitoCalculado == 10) digitoCalculado = 0;
        if (digitoCalculado == 11) digitoCalculado = 1;
        int digitoIngresado = Character.getNumericValue(r.charAt(10));

        boolean valido = digitoCalculado == digitoIngresado;
        if (!valido) {
            return new ResultadoValidacion(false,
                "Dígito verificador inválido. Se esperaba " + digitoCalculado + " pero se ingresó " + digitoIngresado + ".");
        }
        log.debug("[RUC] Validación exitosa para RUC {}", r);
        return new ResultadoValidacion(true, "RUC válido.");
    }

    /**
     * Valida un DNI peruano (8 dígitos numéricos).
     */
    public ResultadoValidacion validarDNI(String dni) {
        if (dni == null) return new ResultadoValidacion(false, "DNI nulo.");
        String d = dni.trim().replaceAll("[^0-9]", "");
        if (d.length() != 8) {
            return new ResultadoValidacion(false, "El DNI debe tener 8 dígitos. Recibido: " + d.length());
        }
        return new ResultadoValidacion(true, "DNI válido.");
    }

    public record ResultadoValidacion(boolean valido, String mensaje) {}
}
