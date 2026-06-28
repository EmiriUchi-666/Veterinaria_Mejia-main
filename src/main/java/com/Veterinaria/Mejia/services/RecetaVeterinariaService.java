package com.Veterinaria.Mejia.services;

import com.Veterinaria.Mejia.models.*;
import com.Veterinaria.Mejia.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class RecetaVeterinariaService {

    private static final Logger log = LoggerFactory.getLogger(RecetaVeterinariaService.class);

    @Autowired private RecetaVeterinariaRepository recetaRepo;
    @Autowired private LineaRecetaRepository lineaRepo;
    @Autowired private PacienteRepository pacienteRepo;
    @Autowired private UsuarioRepository usuarioRepo;

    @Transactional
    public RecetaVeterinaria emitir(Integer pacienteId, Integer veterinarioId,
            String cmp, String diagnostico, String indicaciones,
            List<LineaReceta> lineas) {

        Paciente pac = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
        Usuario vet = usuarioRepo.findById(veterinarioId)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        RecetaVeterinaria r = new RecetaVeterinaria();
        r.setNumeroReceta(generarNumero());
        r.setPaciente(pac);
        r.setVeterinario(vet);
        r.setCmpVeterinario(cmp);
        r.setFechaEmision(LocalDate.now());
        r.setFechaVencimiento(LocalDate.now().plusDays(30));
        r.setDiagnostico(diagnostico);
        r.setIndicaciones(indicaciones);
        r.setEstado(RecetaVeterinaria.EstadoReceta.EMITIDA);

        RecetaVeterinaria guardada = recetaRepo.save(r);

        if (lineas != null) {
            for (LineaReceta l : lineas) {
                l.setReceta(guardada);
                lineaRepo.save(l);
            }
        }
        log.info("[RECETA] {} emitida por {} para paciente {}", 
            guardada.getNumeroReceta(), vet.getNombreUsuario(), pac.getNombre());
        return guardada;
    }

    private String generarNumero() {
        int siguiente = recetaRepo.findMaxNumero()
                .map(n -> Integer.parseInt(n.replace("RV-", "")) + 1)
                .orElse(1);
        return String.format("RV-%07d", siguiente);
    }

    public List<RecetaVeterinaria> listarPorPaciente(Integer pacienteId) {
        return recetaRepo.findByPacienteIdOrderByFechaEmisionDesc(pacienteId);
    }

    public List<RecetaVeterinaria> listarTodas() { return recetaRepo.findAll(); }

    public RecetaVeterinaria obtener(Integer id) {
        return recetaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Receta no encontrada: " + id));
    }

    @Transactional
    public void anular(Integer id) {
        RecetaVeterinaria r = obtener(id);
        r.setEstado(RecetaVeterinaria.EstadoReceta.ANULADA);
        recetaRepo.save(r);
    }
}
