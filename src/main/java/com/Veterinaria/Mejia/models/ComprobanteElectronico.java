package com.Veterinaria.Mejia.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprobante electrónico (Boleta / Factura) emitido a través del PSE Nubefact.
 * Cumple con el reglamento SUNAT para emisión electrónica de MYPES.
 */
@Entity
@Table(name = "comprobantes_electronicos")
public class ComprobanteElectronico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false)
    private TipoComprobante tipoComprobante; // BOLETA, FACTURA

    @Column(nullable = false, length = 6)
    private String serie; // B001 (boleta) o F001 (factura)

    @Column(nullable = false)
    private Integer numero; // correlativo autogenerado

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // ── Receptor ────────────────────────────────────────────────────────────
    @Column(name = "receptor_tipo_doc", length = 2)
    private String receptorTipoDoc = "1"; // 1=DNI, 6=RUC

    @Column(name = "receptor_num_doc", length = 20)
    private String receptorNumDoc;

    @Column(name = "receptor_denominacion", length = 200)
    private String receptorDenominacion;

    @Column(name = "receptor_email", length = 100)
    private String receptorEmail;

    // ── Totales ──────────────────────────────────────────────────────────────
    @Column(name = "total_gravada", precision = 10, scale = 2)
    private BigDecimal totalGravada = BigDecimal.ZERO;

    @Column(name = "total_igv", precision = 10, scale = 2)
    private BigDecimal totalIgv = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "tipo_pago", length = 20)
    private String tipoPago = "CONTADO"; // CONTADO, CREDITO

    @Column(name = "medio_pago", length = 20)
    private String medioPago = "EFECTIVO"; // EFECTIVO, YAPE, TARJETA

    // ── Estado SUNAT ─────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoComprobante estado = EstadoComprobante.PENDIENTE;

    @Column(name = "codigo_hash", length = 100)
    private String codigoHash; // CDR de SUNAT

    @Column(name = "url_pdf", length = 500)
    private String urlPdf; // URL de Nubefact

    @Column(name = "url_xml", length = 500)
    private String urlXml;

    @Column(name = "codigo_respuesta_sunat", length = 10)
    private String codigoRespuestaSunat; // 0 = aceptado

    @Column(name = "descripcion_respuesta", length = 500)
    private String descripcionRespuesta;

    // ── Relaciones ────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleComprobante> detalles;

    public enum TipoComprobante {
        BOLETA("2", "B001", "Boleta de Venta"),
        FACTURA("1", "F001", "Factura");

        public final String codigoSunat;
        public final String serieDefecto;
        public final String label;

        TipoComprobante(String c, String s, String l) {
            this.codigoSunat = c;
            this.serieDefecto = s;
            this.label = l;
        }
    }

    public enum EstadoComprobante {
        PENDIENTE, ENVIADO, ACEPTADO, RECHAZADO, ERROR
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante t) { this.tipoComprobante = t; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate f) { this.fechaEmision = f; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime f) { this.fechaRegistro = f; }
    public String getReceptorTipoDoc() { return receptorTipoDoc; }
    public void setReceptorTipoDoc(String r) { this.receptorTipoDoc = r; }
    public String getReceptorNumDoc() { return receptorNumDoc; }
    public void setReceptorNumDoc(String r) { this.receptorNumDoc = r; }
    public String getReceptorDenominacion() { return receptorDenominacion; }
    public void setReceptorDenominacion(String r) { this.receptorDenominacion = r; }
    public String getReceptorEmail() { return receptorEmail; }
    public void setReceptorEmail(String r) { this.receptorEmail = r; }
    public BigDecimal getTotalGravada() { return totalGravada; }
    public void setTotalGravada(BigDecimal t) { this.totalGravada = t; }
    public BigDecimal getTotalIgv() { return totalIgv; }
    public void setTotalIgv(BigDecimal t) { this.totalIgv = t; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal t) { this.total = t; }
    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String t) { this.tipoPago = t; }
    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String m) { this.medioPago = m; }
    public EstadoComprobante getEstado() { return estado; }
    public void setEstado(EstadoComprobante e) { this.estado = e; }
    public String getCodigoHash() { return codigoHash; }
    public void setCodigoHash(String c) { this.codigoHash = c; }
    public String getUrlPdf() { return urlPdf; }
    public void setUrlPdf(String u) { this.urlPdf = u; }
    public String getUrlXml() { return urlXml; }
    public void setUrlXml(String u) { this.urlXml = u; }
    public String getCodigoRespuestaSunat() { return codigoRespuestaSunat; }
    public void setCodigoRespuestaSunat(String c) { this.codigoRespuestaSunat = c; }
    public String getDescripcionRespuesta() { return descripcionRespuesta; }
    public void setDescripcionRespuesta(String d) { this.descripcionRespuesta = d; }
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public List<DetalleComprobante> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleComprobante> d) { this.detalles = d; }

    /** Número de comprobante formateado: B001-00000001 */
    public String getNumeroCompleto() {
        return serie + "-" + String.format("%08d", numero);
    }
}
