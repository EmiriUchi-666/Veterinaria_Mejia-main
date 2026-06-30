package com.Veterinaria.Mejia.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Cliente;
import com.Veterinaria.Mejia.models.Dueno;
import com.Veterinaria.Mejia.repository.DuenoRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/duenos")
@RequiredArgsConstructor
public class DuenoController {

    private final DuenoRepository duenoRepo;

    @GetMapping
    public String listar(Model model, @RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            model.addAttribute("duenos", duenoRepo.buscar(q.trim()));
            model.addAttribute("q", q);
        } else {
            model.addAttribute("duenos", duenoRepo.findByEstadoTrueOrderByNombreAsc());
        }
        return "duenos/lista-duenos";
    }

    @GetMapping("/nuevo")
    public String formNuevo(Model model) {
        model.addAttribute("dueno", new Dueno());
        return "duenos/form-dueno";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Dueno dueno, RedirectAttributes ra) {
        try {
            // DNI único (si se ingresó)
            if (dueno.getDni() != null && !dueno.getDni().isBlank()) {
                duenoRepo.findByDni(dueno.getDni()).ifPresent(existing -> {
                    if (!existing.getId().equals(dueno.getId())) {
                        throw new IllegalArgumentException("Ya existe un dueño con ese DNI: " + dueno.getDni());
                    }
                });
            }

            // Sincronizar con la entidad Cliente
            Cliente clienteAsociado = dueno.getCliente();
            if (clienteAsociado == null) {
                clienteAsociado = new Cliente();
            }
            clienteAsociado.setNombre(dueno.getNombre());
            clienteAsociado.setTipoDocumento(dueno.getTipoDocumento());
            clienteAsociado.setNumeroDocumento(dueno.getDni());
            clienteAsociado.setTelefono(dueno.getTelefono());
            clienteAsociado.setEmail(dueno.getEmail());
            clienteAsociado.setDireccion(dueno.getDireccion());
            dueno.setCliente(clienteAsociado);

            dueno.setEstado(true);
            duenoRepo.save(dueno);
            ra.addFlashAttribute("successMsg", "Dueño '" + dueno.getNombre() + "' registrado correctamente.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/duenos/nuevo";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al guardar: " + e.getMessage());
            return "redirect:/duenos/nuevo";
        }
        return "redirect:/duenos";
    }

    @GetMapping("/editar/{id}")
    @SuppressWarnings("null")
    public String formEditar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Dueno d = duenoRepo.findById(id).orElse(null);
        if (d == null) { ra.addFlashAttribute("errorMsg", "Dueño no encontrado."); return "redirect:/duenos"; }
        model.addAttribute("dueno", d);
        return "duenos/form-dueno";
    }

    @GetMapping("/eliminar/{id}")
    @SuppressWarnings("null")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        duenoRepo.findById(id).ifPresent(d -> { d.setEstado(false); duenoRepo.save(d); });
        ra.addFlashAttribute("successMsg", "Dueño dado de baja correctamente.");
        return "redirect:/duenos";
    }
}
