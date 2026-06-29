package com.Veterinaria.Mejia.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.Veterinaria.Mejia.dto.DiagnosticoDTO;
import com.Veterinaria.Mejia.dto.ResultadoDiagnosticoDTO;
import com.Veterinaria.Mejia.ia.MotorHeuristico;
import com.Veterinaria.Mejia.ia.ResultadoIA;
import com.Veterinaria.Mejia.models.Producto;
import com.Veterinaria.Mejia.repository.PacienteRepository;
import com.Veterinaria.Mejia.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IAService {

    private final MotorHeuristico motor = new MotorHeuristico();

    private final PacienteRepository pacienteRepo;
    private final ProductoRepository productoRepo;

    public ResultadoDiagnosticoDTO analizar(DiagnosticoDTO dto) {

        // ── Enriquecer con datos del paciente si se seleccionó uno ─────────
        if (dto.getPacienteId() != null) {
            pacienteRepo.findById(dto.getPacienteId()).ifPresent(pac -> {
                dto.setNombreMascota(pac.getNombre());
                if (pac.getEspecie() != null) {
                    dto.setEspecie(pac.getEspecie().getNombreEspecie());
                }
                if (pac.getDueno() != null) {
                    dto.setNombreDueno(pac.getDueno().getNombre());
                    dto.setDuenoId(pac.getDueno().getId());
                }
            });
        }

        // ── Motor heurístico ────────────────────────────────────────────────
        ResultadoIA resultado = motor.evaluar(dto.getTemperatura(), dto.getSintomas());

        ResultadoDiagnosticoDTO salida = new ResultadoDiagnosticoDTO();
        salida.setDiagnostico(resultado.getDiagnostico());
        salida.setPrioridad(resultado.getPrioridad());
        salida.setRecomendacion(resultado.getRecomendacion());

        // ── Alimentos recomendados por especie ──────────────────────────────
        List<Producto> alimentos = obtenerAlimentosPorEspecie(dto.getEspecie());
        salida.setAlimentosRecomendados(alimentos);

        return salida;
    }

    /**
     * Devuelve productos marcados como alimento, filtrados por especie si aplica.
     */
    public List<Producto> obtenerAlimentosPorEspecie(String especie) {
        return productoRepo.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getEstado()))
                .filter(p -> Boolean.TRUE.equals(p.getEsAlimento()))
                .filter(p -> {
                    if (especie == null || especie.isBlank()) return true;
                    if (p.getEspecie() == null) return true; // alimento general
                    return p.getEspecie().getNombreEspecie()
                             .equalsIgnoreCase(especie);
                })
                .collect(Collectors.toList());
    }
}
