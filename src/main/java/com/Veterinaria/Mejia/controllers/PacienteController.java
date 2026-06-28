package com.Veterinaria.Mejia.controllers;

import com.Veterinaria.Mejia.models.*;
import com.Veterinaria.Mejia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired private PacienteRepository pacienteRepo;
    @Autowired private DuenoRepository duenoRepo;
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private EspecieRepository especieRepo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pacientes", pacienteRepo.findByEstadoTrue());
        return "pacientes/lista-pacientes";
    }

    @GetMapping("/nuevo")
    public String formNuevo(Model model) {
        model.addAttribute("paciente", new Paciente());
        model.addAttribute("duenos",   duenoRepo.findByEstadoTrueOrderByNombreAsc());
        model.addAttribute("clientes", clienteRepo.findAll());
        model.addAttribute("especies", especieRepo.findAll());
        return "pacientes/form-paciente";
    }

    @PostMapping("/guardar")
    public String guardar(
            @RequestParam(required = false) Integer id,
            @RequestParam String nombre,
            @RequestParam(required = false) Integer duenoId,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Integer especieId,
            @RequestParam(required = false) String raza,
            @RequestParam(required = false) String colorPelaje,
            @RequestParam(required = false) String microchip,
            @RequestParam(required = false) String sexo,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) Double pesoHistorico,
            @RequestParam(required = false) String alergias,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) String sangre,
            @RequestParam(required = false) Boolean esterilizado,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            RedirectAttributes ra) {

        if (nombre == null || nombre.isBlank()) {
            ra.addFlashAttribute("errorMsg", "El nombre de la mascota es obligatorio.");
            return "redirect:/pacientes/nuevo";
        }

        try {
            Paciente p = (id != null) ? pacienteRepo.findById(id).orElse(new Paciente()) : new Paciente();

            p.setNombre(nombre.trim());
            p.setRaza(raza);
            p.setColorPelaje(colorPelaje);
            p.setMicrochip(microchip != null && !microchip.isBlank() ? microchip.trim() : null);
            p.setSexo(sexo != null && !sexo.isBlank() ? Paciente.SexoPaciente.valueOf(sexo) : Paciente.SexoPaciente.Macho);
            p.setFechaNacimiento(fechaNacimiento != null && !fechaNacimiento.isBlank() ? LocalDate.parse(fechaNacimiento) : null);
            p.setPesoHistorico(pesoHistorico);
            p.setAlergias(alergias);
            p.setObservaciones(observaciones);
            p.setSangre(sangre);
            p.setEsterilizado(Boolean.TRUE.equals(esterilizado));
            p.setEstado(true);

            // Generar N° historia automático
            if (p.getNumeroHistoria() == null || p.getNumeroHistoria().isBlank()) {
                p.setNumeroHistoria("HC-" + System.currentTimeMillis());
            }

            // Dueño (preferido)
            if (duenoId != null) {
                duenoRepo.findById(duenoId).ifPresent(p::setDueno);
            }
            // Especie
            if (especieId != null) {
                especieRepo.findById(especieId).ifPresent(p::setEspecie);
            }
            // Cliente (opcional, para facturación)
            if (clienteId != null) {
                clienteRepo.findById(clienteId).ifPresent(p::setCliente);
            }

            // Foto (opcional, no bloquea el registro)
            if (foto != null && !foto.isEmpty() && foto.getSize() < 5 * 1024 * 1024) {
                try {
                    String ext = "";
                    String original = foto.getOriginalFilename();
                    if (original != null && original.contains(".")) {
                        ext = original.substring(original.lastIndexOf(".")).toLowerCase();
                    }
                    String nombreArchivo = UUID.randomUUID() + ext;
                    Path dir = Paths.get(System.getProperty("user.home"), "vetmejia-uploads", "pacientes");
                    Files.createDirectories(dir);
                    Files.copy(foto.getInputStream(), dir.resolve(nombreArchivo), StandardCopyOption.REPLACE_EXISTING);
                    p.setRutaFoto("/pacientes/" + nombreArchivo);
                } catch (Exception fotoEx) {
                    // Si falla la foto, continuar igual sin imagen
                    System.err.println("Advertencia: no se pudo guardar la foto — " + fotoEx.getMessage());
                }
            }

            pacienteRepo.save(p);
            ra.addFlashAttribute("successMsg", "Paciente '" + p.getNombre() + "' guardado correctamente.");

        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error al guardar: " + e.getMessage());
            return "redirect:/pacientes/nuevo";
        }
        return "redirect:/pacientes";
    }

    @GetMapping("/editar/{id}")
    public String formEditar(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Paciente p = pacienteRepo.findById(id).orElse(null);
        if (p == null) { ra.addFlashAttribute("errorMsg", "Paciente no encontrado."); return "redirect:/pacientes"; }
        model.addAttribute("paciente", p);
        model.addAttribute("duenos",   duenoRepo.findByEstadoTrueOrderByNombreAsc());
        model.addAttribute("clientes", clienteRepo.findAll());
        model.addAttribute("especies", especieRepo.findAll());
        return "pacientes/form-paciente";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes ra) {
        pacienteRepo.findById(id).ifPresent(p -> { p.setEstado(false); pacienteRepo.save(p); });
        ra.addFlashAttribute("successMsg", "Paciente dado de baja correctamente.");
        return "redirect:/pacientes";
    }
}
