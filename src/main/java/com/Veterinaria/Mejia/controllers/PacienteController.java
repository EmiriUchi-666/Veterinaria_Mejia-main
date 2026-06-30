package com.Veterinaria.Mejia.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.Veterinaria.Mejia.models.Cliente;
import com.Veterinaria.Mejia.models.Dueno;
import com.Veterinaria.Mejia.models.HistorialVacuna;
import com.Veterinaria.Mejia.models.Paciente;
import com.Veterinaria.Mejia.repository.CitaRepository;
import com.Veterinaria.Mejia.repository.ClienteRepository;
import com.Veterinaria.Mejia.repository.DuenoRepository;
import com.Veterinaria.Mejia.repository.EspecieRepository;
import com.Veterinaria.Mejia.repository.HistoriaClinicaRepository;
import com.Veterinaria.Mejia.repository.HistorialVacunaRepository;
import com.Veterinaria.Mejia.repository.PacienteRepository;
import com.Veterinaria.Mejia.services.PdfService;
import com.Veterinaria.Mejia.services.RecetaVeterinariaService;
import com.Veterinaria.Mejia.services.TratamientoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteRepository pacienteRepo;
    private final DuenoRepository duenoRepo;
    private final ClienteRepository clienteRepo;
    private final EspecieRepository especieRepo;
    private final HistoriaClinicaRepository historiaRepo;
    private final HistorialVacunaRepository vacunaRepo;
    private final CitaRepository citaRepo;
    private final TratamientoService tratamientoService;
    private final RecetaVeterinariaService recetaService;
    private final PdfService pdfService;

    @GetMapping
    public String listar(Model model) {
        List<Paciente> pacientes = pacienteRepo.findByEstadoTrue();
        Map<Integer, String> edades = new HashMap<>();
        
        for (Paciente p : pacientes) {
            if (p.getFechaNacimiento() != null) {
                Period period = Period.between(p.getFechaNacimiento(), LocalDate.now());
                if (period.getYears() > 0) {
                    edades.put(p.getId(), period.getYears() + (period.getYears() == 1 ? " año" : " años"));
                } else if (period.getMonths() > 0) {
                    edades.put(p.getId(), period.getMonths() + (period.getMonths() == 1 ? " mes" : " meses"));
                } else {
                    edades.put(p.getId(), period.getDays() + (period.getDays() == 1 ? " día" : " días"));
                }
            } else {
                edades.put(p.getId(), "—");
            }
        }

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("edades", edades);
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
            @RequestParam(required = false) Integer duenoId, // El clienteId ya no se recibe del form
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
                Dueno dueno = duenoRepo.findById(duenoId)
                        .orElseThrow(() -> new IllegalArgumentException("Dueño no encontrado."));
                p.setDueno(dueno);

                // Lógica para crear/asignar Cliente automáticamente
                if (dueno.getDni() != null && !dueno.getDni().isBlank()) {
                    Cliente cliente = clienteRepo.findByNumeroDocumento(dueno.getDni())
                            .orElseGet(() -> {
                                Cliente nuevoCliente = new Cliente();
                                nuevoCliente.setNombre(dueno.getNombre());
                                nuevoCliente.setNumeroDocumento(dueno.getDni());
                                nuevoCliente.setTelefono(dueno.getTelefono());
                                nuevoCliente.setDireccion(dueno.getDireccion());
                                return clienteRepo.save(nuevoCliente);
                            });
                    p.setCliente(cliente);
                }
            }
            // Especie
            if (especieId != null) {
                especieRepo.findById(especieId).ifPresent(p::setEspecie);
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

    @GetMapping("/{id}/expediente")
    public String mostrarExpediente(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Paciente paciente = pacienteRepo.findById(id).orElse(null);
        if (paciente == null) {
            ra.addFlashAttribute("errorMsg", "Paciente no encontrado.");
            return "redirect:/pacientes";
        }

        model.addAttribute("paciente", paciente);
        model.addAttribute("historias", historiaRepo.findByPacienteIdOrderByFechaAtencionDesc(id));
        List<HistorialVacuna> vacunas = vacunaRepo.findByPacienteIdOrderByFechaAplicacionDesc(id);
        model.addAttribute("vacunas", vacunas);
        model.addAttribute("tratamientos", tratamientoService.listarPorPaciente(id));
        model.addAttribute("recetas", recetaService.listarPorPaciente(id));
        model.addAttribute("citas", citaRepo.findByPacienteIdOrderByFechaHoraDesc(id));

        return "pacientes/expediente";
    }

    /**
     * FASE 9: Genera y descarga el expediente completo de un paciente en formato PDF.
     */
    @GetMapping("/{id}/expediente/pdf")
    public ResponseEntity<byte[]> generarPdfExpediente(@PathVariable Integer id) {
        Paciente paciente = pacienteRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado para generar PDF"));

        // Cargar todos los datos necesarios para el expediente
        Map<String, Object> variables = new HashMap<>();
        variables.put("paciente", paciente);
        variables.put("historias", historiaRepo.findByPacienteIdOrderByFechaAtencionDesc(id));
        variables.put("vacunas", vacunaRepo.findByPacienteIdOrderByFechaAplicacionDesc(id));
        variables.put("tratamientos", tratamientoService.listarPorPaciente(id));
        variables.put("recetas", recetaService.listarPorPaciente(id));

        byte[] pdfBytes = pdfService.generarPdfDesdeHtml("pdf/expediente-pdf", variables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "expediente-" + paciente.getNombre().replace(" ", "_") + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
