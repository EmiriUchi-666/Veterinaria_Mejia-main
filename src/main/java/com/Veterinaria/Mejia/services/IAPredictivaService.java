package com.Veterinaria.Mejia.services;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Veterinaria.Mejia.models.HistoriaClinica;
import com.Veterinaria.Mejia.models.Paciente;
import com.Veterinaria.Mejia.models.RiesgoPaciente;
import com.Veterinaria.Mejia.repository.HistoriaClinicaRepository;
import com.Veterinaria.Mejia.repository.HistorialVacunaRepository;
import com.Veterinaria.Mejia.repository.PacienteRepository;
import com.Veterinaria.Mejia.repository.RiesgoPacienteRepository;

import lombok.RequiredArgsConstructor;

/**
 * Motor de IA Predictiva basado en reglas heurísticas IF-THEN.
 * Analiza el historial clínico del paciente para estimar riesgos futuros
 * y generar recomendaciones preventivas personalizadas.
 */
@Service
@RequiredArgsConstructor
public class IAPredictivaService {

    private final PacienteRepository pacienteRepo;
    private final HistoriaClinicaRepository historiaClinicaRepo;
    private final HistorialVacunaRepository vacunaRepo;
    private final RiesgoPacienteRepository riesgoRepo;

    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

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

        // FASE 4: Normalización de texto para mejorar la IA
        String raza = normalize(paciente.getRaza());
        String alergias = normalize(paciente.getAlergias());

        // Concatenar diagnósticos históricos para análisis
        StringBuilder historialTexto = new StringBuilder();
        for (HistoriaClinica h : historiales) {
            if (h.getDiagnostico() != null) historialTexto.append(normalize(h.getDiagnostico())).append(" ");
            if (h.getSintomas() != null) historialTexto.append(normalize(h.getSintomas())).append(" ");
        }
        String historial = historialTexto.toString();

        // ── REGLA 1: Razas propensas a problemas articulares ────────
        // Ahora 'pastor aleman' coincide con 'pastor alemán'
        if (raza.contains("pastor aleman") ||
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
                .filter(h -> h.getDiagnostico() != null && normalize(h.getDiagnostico()).contains("infeccion"))
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
        // FASE 4: Corregido para filtrar por el paciente actual.
        long vacunasPendientes = vacunaRepo.findByPacienteId(pacienteId).stream()
                .filter(v -> v.getFechaProximoRefuerzo() != null && v.getFechaProximoRefuerzo().isBefore(LocalDate.now().plusDays(30)))
                .count();
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

    // Método de utilidad para normalizar texto (quitar tildes, a minúsculas)
    private static String normalize(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return DIACRITICS_AND_FRIENDS.matcher(normalized).replaceAll("").toLowerCase();
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
