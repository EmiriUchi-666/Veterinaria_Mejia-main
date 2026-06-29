package com.Veterinaria.Mejia.controllers;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Usuario;
import com.Veterinaria.Mejia.repository.UsuarioRepository;
import com.Veterinaria.Mejia.services.CajaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;
    private final UsuarioRepository usuarioRepo;

    @GetMapping("/estado")
    public String estado(Model model) {
        Optional<com.Veterinaria.Mejia.models.AperturaCierreCaja> abierta = cajaService.getCajaAbierta();
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
    public String abrir(@RequestParam BigDecimal montoInicial, Authentication auth, RedirectAttributes ra) {
        try {
            Usuario u = usuarioRepo.findByNombreUsuario(auth.getName()).orElseThrow();
            cajaService.abrirCaja(u, montoInicial);
            ra.addFlashAttribute("successMsg", "Caja abierta con S/ " + String.format("%.2f", montoInicial));
        } catch (Exception e) { ra.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/caja/estado";
    }

    @GetMapping("/cerrar")
    public String formCerrar(Model model, RedirectAttributes ra) {
        Optional<com.Veterinaria.Mejia.models.AperturaCierreCaja> c = cajaService.getCajaAbierta();
        if (c.isEmpty()) { ra.addFlashAttribute("errorMsg", "No hay caja abierta."); return "redirect:/caja/estado"; }
        model.addAttribute("caja", c.get());
        model.addAttribute("movimientos", cajaService.obtenerMovimientos(c.get().getId()));
        return "caja/form-cerrar";
    }

    @PostMapping("/cerrar")
    public String cerrar(@RequestParam BigDecimal montoFinal,
                         @RequestParam(required = false) String observaciones,
                         RedirectAttributes ra) {
        try {
            com.Veterinaria.Mejia.models.AperturaCierreCaja c = cajaService.cerrarCaja(montoFinal, observaciones);
            BigDecimal dif = c.getDiferencia();
            String msg = "Caja cerrada. Monto final: S/ " + String.format("%.2f", montoFinal);
            if (dif != null && dif.abs().compareTo(new BigDecimal("0.01")) > 0) msg += " | Diferencia: S/ " + String.format("%.2f", dif);
            ra.addFlashAttribute("successMsg", msg);
        } catch (Exception e) { ra.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/caja/estado";
    }

    @PostMapping("/ingreso")
    public String ingreso(@RequestParam BigDecimal monto,
                          @RequestParam(required = false, defaultValue = "Ingreso manual") String concepto,
                          Authentication auth, RedirectAttributes ra) {
        try {
            Usuario u = usuarioRepo.findByNombreUsuario(auth.getName()).orElse(null);
            cajaService.registrarIngreso(monto, concepto, u);
            ra.addFlashAttribute("successMsg", "Ingreso de S/ " + String.format("%.2f", monto) + " registrado.");
        } catch (Exception e) { ra.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/caja/estado";
    }

    @PostMapping("/egreso")
    public String egreso(@RequestParam BigDecimal monto,
                         @RequestParam(required = false, defaultValue = "Egreso manual") String concepto,
                         Authentication auth, RedirectAttributes ra) {
        try {
            Usuario u = usuarioRepo.findByNombreUsuario(auth.getName()).orElse(null);
            cajaService.registrarEgreso(monto, concepto, u);
            ra.addFlashAttribute("successMsg", "Egreso de S/ " + String.format("%.2f", monto) + " registrado.");
        } catch (Exception e) { ra.addFlashAttribute("errorMsg", e.getMessage()); }
        return "redirect:/caja/estado";
    }
}
