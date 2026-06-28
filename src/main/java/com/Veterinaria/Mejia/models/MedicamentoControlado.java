package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A3 — Registro de medicamentos controlados (DIGEMID/SENASA).
 * Exige receta veterinaria digital para categorías 2 y 3.
 */
@Entity
@Table(name = "medicamentos_controlados")
public class MedicamentoControlado {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_digemid", nullable = false)
    private CategoriaMedicamento categoria;

    @Column(name = "principio_activo", length = 200)
    private String principioActivo;

    @Column(name = "registro_sanitario", length = 50)
    private String registroSanitario;

    @Column(name = "laboratorio_fabricante", length = 150)
    private String laboratorioFabricante;

    @Column(name = "requiere_receta", nullable = false)
    private Boolean requiereReceta = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id")
    private RecetaVeterinaria receta;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "observaciones_digemid", length = 500)
    private String observacionesDigemid;

    public enum CategoriaMedicamento {
        LIBRE("Venta libre sin receta"),
        RECETA("Receta veterinaria obligatoria"),
        CONTROLADO("Medicamento controlado DIGEMID — Registro especial requerido");

        public final String descripcion;
        CategoriaMedicamento(String d) { this.descripcion = d; }
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto p) { this.producto = p; }
    public CategoriaMedicamento getCategoria() { return categoria; }
    public void setCategoria(CategoriaMedicamento c) { this.categoria = c; }
    public String getPrincipioActivo() { return principioActivo; }
    public void setPrincipioActivo(String p) { this.principioActivo = p; }
    public String getRegistroSanitario() { return registroSanitario; }
    public void setRegistroSanitario(String r) { this.registroSanitario = r; }
    public String getLaboratorioFabricante() { return laboratorioFabricante; }
    public void setLaboratorioFabricante(String l) { this.laboratorioFabricante = l; }
    public Boolean getRequiereReceta() { return requiereReceta; }
    public void setRequiereReceta(Boolean r) { this.requiereReceta = r; }
    public RecetaVeterinaria getReceta() { return receta; }
    public void setReceta(RecetaVeterinaria r) { this.receta = r; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime f) { this.fechaRegistro = f; }
    public String getObservacionesDigemid() { return observacionesDigemid; }
    public void setObservacionesDigemid(String o) { this.observacionesDigemid = o; }
}
