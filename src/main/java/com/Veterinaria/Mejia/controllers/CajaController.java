package com.Veterinaria.Mejia.controllers;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.AperturaCierreCaja;
import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.UsuarioRepository;
import com.Veterinaria.Mejia.services.CajaService;

import lombok.RequiredArgsConstructor;

/**
 * FASE 7: ya no expone /caja/ingreso ni /caja/egreso. El saldo de caja se
 * alimenta automáticamente desde VentaCreacionService (ventas en efectivo)
 * e IngresoStockService (compras al contado). Ver CajaService para el
 * detalle de ambos flujos.
 */
@Controller
@RequestMapping("/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;
    private final UsuarioRepository usuarioRepo;

    @GetMapping("/estado")
    public String estado(Model model) {
        Optional<AperturaCierreCaja> abierta = cajaService.getCajaAbierta();
        model.addAttribute("cajaAbierta", abierta.orElse(null));
        model.addAttribute("hayCaja", abierta.isPresent());
        model.addAttribute("historial", cajaService.obtenerHistorial());
        if (abierta.isPresent()) {
            model.addAttribute("movimientos", cajaService.obtenerMovimientos(abierta.get().getId()));
        }
        return "caja/estado";
    }

    @GetMapping("/abrir")
    public String formAbrir(Model model) {
        if (cajaService.hayCajaAbierta()) model.addAttribute("errorMsg", "Ya hay una caja abierta.");
        return "caja/form-abrir";
    }

    @PostMapping("/abrir")
    public String abrir(@RequestParam BigDecimal montoInicial,
                         @RequestParam(required = false) LocalTime horaCierre,
                         Authentication auth, RedirectAttributes ra) {
        try {
            Usuario u = usuarioRepo.findByNombreUsuario(auth.getName()).orElseThrow();
            cajaService.abrirCaja(u, montoInicial, horaCierre);
            String msg = "Caja abierta con S/ " + String.format("%.2f", montoInicial);
            if (horaCierre != null) {
                msg += ". Se cerrará automáticamente a las " + horaCierre;
            }
            ra.addFlashAttribute("successMsg", msg);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/caja/estado";
    }

    @GetMapping("/cerrar")
    public String formCerrar(Model model, RedirectAttributes ra) {
        Optional<AperturaCierreCaja> c = cajaService.getCajaAbierta();
        if (c.isEmpty()) {
            ra.addFlashAttribute("errorMsg", "No hay caja abierta.");
            return "redirect:/caja/estado";
        }
        model.addAttribute("caja", c.get());
        model.addAttribute("movimientos", cajaService.obtenerMovimientos(c.get().getId()));
        return "caja/form-cerrar";
    }

    @PostMapping("/cerrar")
    public String cerrar(RedirectAttributes ra) {
        try {
            AperturaCierreCaja c = cajaService.cerrarCaja();
            String msg = "Caja cerrada. Saldo final: S/ " + String.format("%.2f", c.getMontoFinal());
            ra.addFlashAttribute("successMsg", msg);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/caja/estado";
    }
}