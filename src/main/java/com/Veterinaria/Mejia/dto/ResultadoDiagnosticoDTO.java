package com.Veterinaria.Mejia.dto;

import com.Veterinaria.Mejia.models.Producto;
import java.util.List;

public class ResultadoDiagnosticoDTO {
    private String diagnostico;
    private int prioridad;
    private String recomendacion;
    private List<Producto> alimentosRecomendados;

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String d) { this.diagnostico = d; }
    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int p) { this.prioridad = p; }
    public String getRecomendacion() { return recomendacion; }
    public void setRecomendacion(String r) { this.recomendacion = r; }
    public List<Producto> getAlimentosRecomendados() { return alimentosRecomendados; }
    public void setAlimentosRecomendados(List<Producto> a) { this.alimentosRecomendados = a; }
}
