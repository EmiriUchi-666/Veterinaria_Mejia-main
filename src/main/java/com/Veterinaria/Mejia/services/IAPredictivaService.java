package com.Veterinaria.Mejia.services;

import com.Veterinaria.Mejia.models.*;
import com.Veterinaria.Mejia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor de IA Predictiva basado en reglas heurísticas IF-THEN.
 * Analiza el historial clínico del paciente para estimar riesgos futuros
 * y generar recomendaciones preventivas personalizadas.
 */
@Service
public class IAPredictivaService {

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepo;

    @Autowired
    private HistorialVacunaRepository vacunaRepo;

    @Autowired
    private RiesgoPacienteRepository riesgoRepo;

    /**
     * Evalúa el riesgo predictivo de un paciente y lo persiste.
     *
     * @param pacienteId ID del paciente a evaluar
     * @return RiesgoPaciente con los scores calculados
     */
    @Transactional
    public RiesgoPaciente evaluarRiesgoPaciente(Integer pacienteId) {
        Paciente paciente = pacienteRepo.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + pacienteId));

        List<HistoriaClinica> historiales = historiaClinicaRepo
                .findByPacienteIdOrderByFechaAtencionDesc(pacienteId);

        RiesgoPaciente riesgo = new RiesgoPaciente();
        riesgo.setPaciente(paciente);

        List<String> enfermedadesProbables = new ArrayList<>();
        List<String> recomendaciones = new ArrayList<>();

        // ── CALCULAR EDAD ──────────────────────────────────────────
        int edadAnios = 0;
        if (paciente.getFechaNacimiento() != null) {
            edadAnios = Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears();
        }

        String raza = paciente.getRaza() != null ? paciente.getRaza().toLowerCase() : "";
        String alergias = paciente.getAlergias() != null ? paciente.getAlergias().toLowerCase() : "";

        // Concatenar diagnósticos históricos para análisis
        StringBuilder historialTexto = new StringBuilder();
        for (HistoriaClinica h : historiales) {
            if (h.getDiagnostico() != null) historialTexto.append(h.getDiagnostico().toLowerCase()).append(" ");
            if (h.getSintomas() != null) historialTexto.append(h.getSintomas().toLowerCase()).append(" ");
        }
        String historial = historialTexto.toString();

        // ── REGLA 1: Razas propensas a problemas articulares ────────
        if (raza.contains("pastor alemán") || raza.contains("pastor aleman") ||
                raza.contains("labrador") || raza.contains("golden") ||
                raza.contains("rottweiler") || raza.contains("bernés")) {
            riesgo.setRiesgoArticular(75.0);
            enfermedadesProbables.add("Displasia de cadera / Problemas articulares");
            recomendaciones.add("Control radiológico anual de articulaciones");
            recomendaciones.add("Suplementar con condroprotectores preventivos");
        }

        // ── REGLA 2: Pacientes geriátricos (> 7 años) ───────────────
        if (edadAnios > 7) {
            riesgo.setRiesgoCardiaco(60.0);
            riesgo.setRiesgoEnfermedadesRecurrentes(riesgo.getRiesgoEnfermedadesRecurrentes() + 20);
            enfermedadesProbables.add("Enfermedades cardíacas / Deterioro renal");
            recomendaciones.add("Chequeo cardiológico semestral obligatorio");
            recomendaciones.add("Perfil bioquímico renal cada 6 meses");
        }

        // ── REGLA 3: Historial de alergias ──────────────────────────
        if (!alergias.isEmpty() || historial.contains("alergi")) {
            riesgo.setRiesgoAlergico(80.0);
            enfermedadesProbables.add("Dermatitis alérgica recurrente");
            recomendaciones.add("Evitar alérgenos identificados: " + paciente.getAlergias());
            recomendaciones.add("Tener antihistamínico de emergencia disponible");
        }

        // ── REGLA 4: Historial de infecciones recurrentes ───────────
        long infecciones = historiales.stream()
                .filter(h -> h.getDiagnostico() != null &&
                        (h.getDiagnostico().toLowerCase().contains("infección") ||
                         h.getDiagnostico().toLowerCase().contains("infeccion")))
                .count();
        if (infecciones >= 2) {
            riesgo.setRiesgoEnfermedadesRecurrentes(riesgo.getRiesgoEnfermedadesRecurrentes() + 40);
            riesgo.setRiesgoRecaida(70.0);
            enfermedadesProbables.add("Infecciones recurrentes (patrón detectado)");
            recomendaciones.add("Evaluación inmunológica preventiva");
        }

        // ── REGLA 5: No esterilizado (riesgo oncológico) ────────────
        if (paciente.getEsterilizado() != null && !paciente.getEsterilizado() && edadAnios > 3) {
            riesgo.setRiesgoEnfermedadesRecurrentes(riesgo.getRiesgoEnfermedadesRecurrentes() + 15);
            enfermedadesProbables.add("Tumores mamarios / Patología prostática");
            recomendaciones.add("Evaluar esterilización para reducir riesgo oncológico");
        }

        // ── REGLA 6: Vacunas vencidas ────────────────────────────────
        long vacunasPendientes = vacunaRepo.contarVacunasPendientes(LocalDate.now(), LocalDate.now().plusDays(30));
        if (vacunasPendientes > 0) {
            recomendaciones.add("Actualizar calendario de vacunación urgente (" + vacunasPendientes + " vacuna(s) próximas a vencer)");
        }

        // ── CÁLCULO DEL NIVEL DE RIESGO GENERAL (1-10) ──────────────
        double promedioRiesgo = (riesgo.getRiesgoArticular()
                + riesgo.getRiesgoCardiaco()
                + riesgo.getRiesgoAlergico()
                + riesgo.getRiesgoEnfermedadesRecurrentes()
                + riesgo.getRiesgoRecaida()) / 5.0;

        int nivelGeneral = (int) Math.min(10, Math.max(1, promedioRiesgo / 10));
        riesgo.setNivelRiesgoGeneral(nivelGeneral);

        // ── GUARDAR RESULTADOS ───────────────────────────────────────
        riesgo.setEnfermedadesFuturasProbables(String.join(" | ", enfermedadesProbables));
        riesgo.setRecomendaciones(String.join("\n• ", recomendaciones));

        // Eliminar evaluación previa y guardar nueva
        riesgoRepo.findFirstByPacienteIdOrderByFechaEvaluacionDesc(pacienteId)
                .ifPresent(r -> riesgoRepo.delete(r));

        return riesgoRepo.save(riesgo);
    }

    /** Retorna la última evaluación de riesgo de un paciente (sin re-calcular). */
    public RiesgoPaciente obtenerUltimoRiesgo(Integer pacienteId) {
        return riesgoRepo.findFirstByPacienteIdOrderByFechaEvaluacionDesc(pacienteId)
                .orElse(null);
    }

    /** Cuenta los pacientes con nivel de riesgo alto (>= 7). */
    public long contarCasosAltoRiesgo() {
        return riesgoRepo.countByNivelRiesgoGeneralGreaterThanEqual(7);
    }
}
