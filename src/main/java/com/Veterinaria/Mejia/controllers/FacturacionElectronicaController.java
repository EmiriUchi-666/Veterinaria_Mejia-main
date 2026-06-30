package com.Veterinaria.Mejia.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.ComprobanteElectronico;
import com.Veterinaria.Mejia.models.FacturacionEstado;
import com.Veterinaria.Mejia.repository.FacturacionEstadoRepository;
import com.Veterinaria.Mejia.repository.VentaRepository;
import com.Veterinaria.Mejia.services.FacturacionElectronicaService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/facturacion")
@RequiredArgsConstructor
public class FacturacionElectronicaController {

    private final FacturacionElectronicaService facturacionService;
    private final FacturacionEstadoRepository facturacionEstadoRepository;
    private final VentaRepository ventaRepo;

    @Value("${nubefact.modo.prueba:true}") private boolean modoPrueba;
    @Value("${nubefact.ruc:20600000001}") private String rucEmisor;
    @Value("${nubefact.razon.social:VETERINARIA MEJIA E.I.R.L.}") private String razonSocial;

    /** GET /facturacion/historial — Historial de comprobantes */
    @GetMapping
    public String mostrarHistorial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            Model model) {

        Page<FacturacionEstado> paginaEstados = facturacionEstadoRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        model.addAttribute("paginaEstados", paginaEstados);
        model.addAttribute("rucEmisor", rucEmisor);
        return "facturacion/historial";
    }


    /** GET /facturacion/emitir/{ventaId} — Formulario de emisión */
    @SuppressWarnings("null")
    @GetMapping("/emitir/{ventaId}")
    public String formEmitir(@PathVariable Integer ventaId, Model model, RedirectAttributes ra) {
        var venta = ventaRepo.findById(ventaId).orElse(null);
        if (venta == null) {
            ra.addFlashAttribute("errorMsg", "Venta no encontrada.");
            return "redirect:/facturacion";
        }
        model.addAttribute("venta", venta);
        model.addAttribute("modoPrueba", modoPrueba);
        // Pre-cargar datos del cliente si tiene uno asignado
        if (venta.getCliente() != null) {
            model.addAttribute("numDocPreloaded", venta.getCliente().getNumeroDocumento());
            model.addAttribute("nombrePreloaded", venta.getCliente().getNombre());
        }
        return "facturacion/emitir-boleta";
    }

    /** POST /facturacion/boleta — Emite boleta */
    @SuppressWarnings("null")
    @PostMapping("/boleta")
    public String emitirBoleta(
            @RequestParam Integer ventaId,
            @RequestParam(required = false) String dniReceptor,
            @RequestParam(required = false) String nombreReceptor,
            @RequestParam(required = false) String emailReceptor,
            @RequestParam(defaultValue = "EFECTIVO") String medioPago,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            ComprobanteElectronico comp = facturacionService.emitirBoleta(
                    ventaId,
                    dniReceptor,
                    nombreReceptor,
                    emailReceptor,
                    auth.getName(),
                    medioPago);
            ra.addFlashAttribute("successMsg",
                    "✅ " + comp.getNumeroCompleto() + " emitida correctamente. Estado: " + comp.getEstado());
            if (comp.getUrlPdf() != null) {
                ra.addFlashAttribute("urlPdf", comp.getUrlPdf());
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al emitir boleta: " + e.getMessage());
        }
        return "redirect:/facturacion";
    }

    /** POST /facturacion/factura — Emite factura */
    @SuppressWarnings("null")
    @PostMapping("/factura")
    public String emitirFactura(
            @RequestParam Integer ventaId,
            @RequestParam String rucReceptor,
            @RequestParam String razonSocialReceptor,
            @RequestParam(required = false) String emailReceptor,
            @RequestParam(defaultValue = "EFECTIVO") String medioPago,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            ComprobanteElectronico comp = facturacionService.emitirFactura(
                    ventaId, rucReceptor, razonSocialReceptor, emailReceptor, auth.getName(), medioPago);
            ra.addFlashAttribute("successMsg",
                    "✅ " + comp.getNumeroCompleto() + " emitida correctamente. Estado: " + comp.getEstado());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al emitir factura: " + e.getMessage());
        }
        return "redirect:/facturacion";
    }

    /** POST /facturacion/reenviar/{id} — Reintenta envío a SUNAT */
    @SuppressWarnings("null")
    @PostMapping("/reenviar/{id}")
    public String reenviar(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            var comp = facturacionService.reenviar(id);
            ra.addFlashAttribute("successMsg", "Comprobante " + comp.getNumeroCompleto() + " reenviado. Estado: " + comp.getEstado());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al reenviar: " + e.getMessage());
        }
        return "redirect:/facturacion";
    }

    /** POST /facturacion/reenviar-email/{id} — Reenvía el comprobante por correo. */
    @PostMapping("/reenviar-email/{id}")
    public String reenviarEmail(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            facturacionService.reenviarPorEmail(id);
            ra.addFlashAttribute("successMsg", "El comprobante ha sido reenviado por correo electrónico.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al reenviar por correo: " + e.getMessage());
        }
        return "redirect:/facturacion";
    }

    /** GET /facturacion/detalle/{id} — Ver detalle de comprobante (API JSON) */
    @GetMapping("/api/detalle/{id}")
    @ResponseBody
    public ResponseEntity<?> detalleApi(@PathVariable Integer id) {
        return facturacionService.obtenerHistorial().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .map(c -> ResponseEntity.ok(Map.of(
                        "id", c.getId(),
                        "numero", c.getNumeroCompleto(),
                        "tipo", c.getTipoComprobante().label,
                        "receptor", c.getReceptorDenominacion(),
                        "total", c.getTotal(),
                        "estado", c.getEstado(),
                        "urlPdf", c.getUrlPdf() != null ? c.getUrlPdf() : "",
                        "hash", c.getCodigoHash() != null ? c.getCodigoHash() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}