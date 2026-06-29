package com.Veterinaria.Mejia.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.services.CRMService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador del módulo CRM (Customer Relationship Management).
 * Segmentación de clientes, métricas y alertas de retención.
 */
@Controller
@RequestMapping("/crm")
@PreAuthorize("hasAnyAuthority('ROLE_Administrador')")
@RequiredArgsConstructor
public class CRMController {

    private final CRMService crmService;

    /**
     * GET /crm/dashboard — Dashboard principal con resumen de segmentos.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("segmentos",         crmService.listarSegmentos());
        model.addAttribute("topClientes",       crmService.obtenerTopClientes(10));
        model.addAttribute("clientesInactivos", crmService.obtenerClientesInactivos(60));
        model.addAttribute("vip",               crmService.obtenerPorSegmento("VIP"));
        model.addAttribute("frecuentes",        crmService.obtenerPorSegmento("Frecuente"));
        model.addAttribute("ocasionales",       crmService.obtenerPorSegmento("Ocasional"));
        model.addAttribute("inactivos",         crmService.obtenerPorSegmento("Inactivo"));
        return "crm/dashboard";
    }

    /**
     * GET /crm/clientes/inactivos — Lista clientes sin visitar en los últimos N días.
     */
    @GetMapping("/clientes/inactivos")
    public String clientesInactivos(
            @RequestParam(defaultValue = "60") Integer dias,
            Model model) {
        model.addAttribute("clientes", crmService.obtenerClientesInactivos(dias));
        model.addAttribute("diasFiltro", dias);
        return "crm/clientes-inactivos";
    }

    /**
     * GET /crm/clientes/top — Ranking de mejores clientes por gasto.
     */
    @GetMapping("/clientes/top")
    public String topClientes(
            @RequestParam(defaultValue = "10") Integer limite,
            Model model) {
        model.addAttribute("clientes", crmService.obtenerTopClientes(limite));
        model.addAttribute("limite", limite);
        return "crm/top-clientes";
    }

    /**
     * POST /crm/recalcular — Recalcula las métricas de todos los clientes.
     */
    @PostMapping("/recalcular")
    public String recalcularMetricas(RedirectAttributes ra) {
        try {
            crmService.recalcularTodosLosClientes();
            ra.addFlashAttribute("successMsg", "Métricas CRM recalculadas para todos los clientes.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al recalcular: " + e.getMessage());
        }
        return "redirect:/crm/dashboard";
    }
}
