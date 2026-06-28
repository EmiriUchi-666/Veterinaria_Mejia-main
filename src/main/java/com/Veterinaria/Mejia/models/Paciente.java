package com.Veterinaria.Mejia.models;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_historia", unique = true, length = 20)
    private String numeroHistoria;

    /** Dueño/tutor de la mascota (contacto clínico) */
    @ManyToOne
    @JoinColumn(name = "dueno_id")
    private Dueno dueno;

    /** Cliente para facturación (puede ser distinto al dueño) */
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "especie_id")
    private Especie especie;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String raza;

    @Column(name = "color_pelaje", length = 50)
    private String colorPelaje;

    @Column(unique = true, length = 50)
    private String microchip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SexoPaciente sexo = SexoPaciente.Macho;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "peso_historico")
    private Double pesoHistorico;

    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "ruta_foto", length = 255)
    private String rutaFoto;

    private Boolean esterilizado = false;

    @Column(length = 10)
    private String sangre;

    @Column(nullable = false)
    private Boolean estado = true;

    public enum SexoPaciente { Macho, Hembra }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNumeroHistoria() { return numeroHistoria; }
    public void setNumeroHistoria(String n) { this.numeroHistoria = n; }
    public Dueno getDueno() { return dueno; }
    public void setDueno(Dueno dueno) { this.dueno = dueno; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente c) { this.cliente = c; }
    public Especie getEspecie() { return especie; }
    public void setEspecie(Especie e) { this.especie = e; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public String getColorPelaje() { return colorPelaje; }
    public void setColorPelaje(String c) { this.colorPelaje = c; }
    public String getMicrochip() { return microchip; }
    public void setMicrochip(String m) { this.microchip = m; }
    public SexoPaciente getSexo() { return sexo; }
    public void setSexo(SexoPaciente sexo) { this.sexo = sexo; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate f) { this.fechaNacimiento = f; }
    public Double getPesoHistorico() { return pesoHistorico; }
    public void setPesoHistorico(Double p) { this.pesoHistorico = p; }
    public String getAlergias() { return alergias; }
    public void setAlergias(String a) { this.alergias = a; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String o) { this.observaciones = o; }
    public String getRutaFoto() { return rutaFoto; }
    public void setRutaFoto(String r) { this.rutaFoto = r; }
    public Boolean getEsterilizado() { return esterilizado; }
    public void setEsterilizado(Boolean e) { this.esterilizado = e; }
    public String getSangre() { return sangre; }
    public void setSangre(String s) { this.sangre = s; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
}
