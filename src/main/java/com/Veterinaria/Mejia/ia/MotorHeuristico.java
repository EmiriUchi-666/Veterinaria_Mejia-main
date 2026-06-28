package com.Veterinaria.Mejia.ia;

import java.util.*;

/**
 * Motor heurístico expandido (de 4 a 20+ reglas).
 * Actúa como fallback cuando hay menos de 100 casos clínicos.
 * CDSS Phase 1: Rule-based engine ampliado con evidencia clínica.
 */
public class MotorHeuristico {

    private final List<ReglaDiagnostico> reglas;

    public MotorHeuristico() {
        reglas = new ArrayList<>();

        // ── Enfermedades infecciosas graves ──────────────────────────────────
        reglas.add(new ReglaDiagnostico("Posible Parvovirus Canino",     "diarrea",      39.0, 10, "Hospitalización inmediata + fluidoterapia IV"));
        reglas.add(new ReglaDiagnostico("Posible Moquillo Canino",       "tos",          39.5,  9, "Aislamiento + soporte inmunológico urgente"));
        reglas.add(new ReglaDiagnostico("Posible Leptospirosis",         "ictericia",    39.0,  9, "Penicilina + fluidoterapia urgente"));
        reglas.add(new ReglaDiagnostico("Posible Distemper Felino",      "estornudo",    39.5,  8, "Antiviral + soporte nutricional"));
        reglas.add(new ReglaDiagnostico("Posible Toxoplasma",            "convulsion",   38.5,  8, "Clindamicina + neurológico urgente"));
        reglas.add(new ReglaDiagnostico("Posible Rabia (descarte)",      "agresividad",  38.0, 10, "Aislamiento INMEDIATO + notificación SENASA"));

        // ── Enfermedades parasitarias ─────────────────────────────────────────
        reglas.add(new ReglaDiagnostico("Posible Ehrlichiosis",          "petequias",    39.0,  8, "Doxiciclina + hemograma urgente"));
        reglas.add(new ReglaDiagnostico("Posible Leishmaniasis",         "alopecia",     38.5,  7, "Antimoniales + derivar especialista"));
        reglas.add(new ReglaDiagnostico("Parasitosis Gastrointestinal",  "parasitos",    37.5,  5, "Antiparasitario + coproparasitológico"));

        // ── Problemas digestivos y metabólicos ─────────────────────────────
        reglas.add(new ReglaDiagnostico("Gastroenteritis Aguda",         "vomito",       38.0,  5, "Ayuno 12h + rehidratación oral"));
        reglas.add(new ReglaDiagnostico("Pancreatitis",                  "abdomen",      39.0,  7, "Ayuno + fluidoterapia IV + analgesia"));
        reglas.add(new ReglaDiagnostico("Insuficiencia Renal Aguda",     "polidipsia",   38.0,  7, "Hemoquímica urgente + fluidoterapia"));
        reglas.add(new ReglaDiagnostico("Diabetes Mellitus",             "poliuria",     37.5,  5, "Glucosa en sangre + insulina si confirma"));
        reglas.add(new ReglaDiagnostico("Hipoglicemia",                  "debilidad",    36.0,  6, "Glucosa IV/oral urgente"));

        // ── Problemas respiratorios ─────────────────────────────────────────
        reglas.add(new ReglaDiagnostico("Neumonía Bacteriana",           "disnea",       39.5,  8, "Antibiótico sistémico + radiografía tórax"));
        reglas.add(new ReglaDiagnostico("Asma Felina",                   "sibilancias",  38.0,  6, "Broncodilatador + corticoide"));
        reglas.add(new ReglaDiagnostico("Traqueobronquitis Infecciosa",  "ronquera",     38.5,  6, "Antitusivo + antibiótico si bacteriano"));

        // ── Problemas musculoesqueléticos ───────────────────────────────────
        reglas.add(new ReglaDiagnostico("Lesión Ortopédica / Fractura",  "cojera",       37.0,  6, "Radiografía urgente + inmovilización"));
        reglas.add(new ReglaDiagnostico("Displasia de Cadera",           "dificultad",   37.5,  5, "Radiografía pelvis + condroprotectores"));

        // ── Problemas dérmicos ──────────────────────────────────────────────
        reglas.add(new ReglaDiagnostico("Dermatitis Alérgica",           "prurito",      37.5,  4, "Antihistamínico + valorar alérgeno"));
        reglas.add(new ReglaDiagnostico("Sarna Sarcóptica",              "costras",      37.5,  5, "Ivermectina + baños medicados"));
    }

    /**
     * Evalúa síntomas y temperatura.
     * Devuelve top-3 diagnósticos ordenados por prioridad.
     */
    public List<ResultadoIA> evaluarTop3(Double temperatura, String sintomas) {
        if (sintomas == null) sintomas = "";
        final String sintomasLower = sintomas.toLowerCase();
        final double temp = temperatura != null ? temperatura : 38.5;

        List<ResultadoIA> coincidencias = new ArrayList<>();
        for (ReglaDiagnostico regla : reglas) {
            if (sintomasLower.contains(regla.getSintomaClave()) && temp >= regla.getTemperaturaMinima()) {
                coincidencias.add(new ResultadoIA(regla.getEnfermedad(), regla.getPrioridad(), regla.getRecomendacion()));
            }
        }
        coincidencias.sort((a, b) -> Integer.compare(b.getPrioridad(), a.getPrioridad()));
        if (coincidencias.isEmpty()) {
            coincidencias.add(new ResultadoIA("Examen clínico general requerido", 3,
                    "No se identificó patrón específico. Evaluación veterinaria completa necesaria."));
        }
        return coincidencias.subList(0, Math.min(3, coincidencias.size()));
    }

    /** Compatibilidad retroactiva — retorna el primer resultado */
    public ResultadoIA evaluar(Double temperatura, String sintomas) {
        return evaluarTop3(temperatura, sintomas).get(0);
    }
}
