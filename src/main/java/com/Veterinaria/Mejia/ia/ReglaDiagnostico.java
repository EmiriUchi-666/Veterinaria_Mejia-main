package com.Veterinaria.Mejia.ia;

public class ReglaDiagnostico {

    private String enfermedad;
    private String sintomaClave;
    private Double temperaturaMinima;
    private Integer prioridad;
    private String recomendacion;

    public ReglaDiagnostico(
            String enfermedad,
            String sintomaClave,
            Double temperaturaMinima,
            Integer prioridad,
            String recomendacion) {

        this.enfermedad = enfermedad;
        this.sintomaClave = sintomaClave;
        this.temperaturaMinima = temperaturaMinima;
        this.prioridad = prioridad;
        this.recomendacion = recomendacion;
    }

    public String getEnfermedad() {
        return enfermedad;
    }

    public String getSintomaClave() {
        return sintomaClave;
    }

    public Double getTemperaturaMinima() {
        return temperaturaMinima;
    }

    public Integer getPrioridad() {
        return prioridad;
    }

    public String getRecomendacion() {
        return recomendacion;
    }
}