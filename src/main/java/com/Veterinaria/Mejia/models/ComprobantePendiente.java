package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Cola de contingencia para comprobantes no enviados a SUNAT.
 * Permite operar hasta 72 horas sin conexión (norma SUNAT).
 */
@Entity
@Table(name = "comprobantes_pendientes")
public class ComprobantePendiente {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_id")
    private ComprobanteElectronico comprobante;

    @Column(name = "xml_generado", columnDefinition = "LONGTEXT")
    private String xmlGenerado;

    @Column(name = "json_request", columnDefinition = "TEXT")
    private String jsonRequest;

    @Column(name = "intentos")
    private Integer intentos = 0;

    @Column(name = "ultimo_intento")
    private LocalDateTime ultimoIntento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPendiente estado = EstadoPendiente.PENDIENTE;

    @Column(name = "error_detalle", length = 500)
    private String errorDetalle;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public enum EstadoPendiente { PENDIENTE, ENVIANDO, ENVIADO, ERROR_PERMANENTE }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public ComprobanteElectronico getComprobante() { return comprobante; }
    public void setComprobante(ComprobanteElectronico c) { this.comprobante = c; }
    public String getXmlGenerado() { return xmlGenerado; }
    public void setXmlGenerado(String x) { this.xmlGenerado = x; }
    public String getJsonRequest() { return jsonRequest; }
    public void setJsonRequest(String j) { this.jsonRequest = j; }
    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer i) { this.intentos = i; }
    public LocalDateTime getUltimoIntento() { return ultimoIntento; }
    public void setUltimoIntento(LocalDateTime u) { this.ultimoIntento = u; }
    public EstadoPendiente getEstado() { return estado; }
    public void setEstado(EstadoPendiente e) { this.estado = e; }
    public String getErrorDetalle() { return errorDetalle; }
    public void setErrorDetalle(String e) { this.errorDetalle = e; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }
}
