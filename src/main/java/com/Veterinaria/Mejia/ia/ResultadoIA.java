package com.Veterinaria.Mejia.ia;

public class ResultadoIA {

    private String diagnostico;
    private Integer prioridad;
    private String recomendacion;

    public ResultadoIA(
            String diagnostico,
            Integer prioridad,
            String recomendacion) {

        this.diagnostico = diagnostico;
        this.prioridad = prioridad;
        this.recomendacion = recomendacion;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public Integer getPrioridad() {
        return prioridad;
    }

    public String getRecomendacion() {
        return recomendacion;
    }
}