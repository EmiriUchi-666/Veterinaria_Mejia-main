package com.Veterinaria.Mejia.dto;

public class DiagnosticoDTO {
    private Double temperatura;
    private String sintomas;
    private Double peso;
    private String especie;
    private Integer pacienteId;
    private Integer duenoId;
    private String nombreMascota;
    private String nombreDueno;

    public Double getTemperatura() { return temperatura; }
    public void setTemperatura(Double t) { this.temperatura = t; }
    public String getSintomas() { return sintomas; }
    public void setSintomas(String s) { this.sintomas = s; }
    public Double getPeso() { return peso; }
    public void setPeso(Double p) { this.peso = p; }
    public String getEspecie() { return especie; }
    public void setEspecie(String e) { this.especie = e; }
    public Integer getPacienteId() { return pacienteId; }
    public void setPacienteId(Integer id) { this.pacienteId = id; }
    public Integer getDuenoId() { return duenoId; }
    public void setDuenoId(Integer id) { this.duenoId = id; }
    public String getNombreMascota() { return nombreMascota; }
    public void setNombreMascota(String n) { this.nombreMascota = n; }
    public String getNombreDueno() { return nombreDueno; }
    public void setNombreDueno(String n) { this.nombreDueno = n; }
}
