package com.Veterinaria.Mejia.facturacion;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public class NubefactRequestDTO {
    @JsonProperty("operacion")                       public String operacion = "generar_comprobante";
    @JsonProperty("tipo_de_comprobante")             public int tipoDeComprobante; // 1=Factura 2=Boleta
    @JsonProperty("serie")                           public String serie;
    @JsonProperty("numero")                          public int numero;
    @JsonProperty("sunat_transaction")               public int sunatTransaction = 1;
    @JsonProperty("cliente_tipo_de_documento")       public int clienteTipoDeDocumento; // 1=DNI 6=RUC
    @JsonProperty("cliente_numero_de_documento")     public String clienteNumeroDeDocumento;
    @JsonProperty("cliente_denominacion")            public String clienteDenominacion;
    @JsonProperty("cliente_direccion")               public String clienteDireccion = "";
    @JsonProperty("cliente_email")                   public String clienteEmail = "";
    @JsonProperty("cliente_email_1")                 public String clienteEmail1 = "";
    @JsonProperty("fecha_de_emision")                public String fechaDeEmision;
    @JsonProperty("fecha_de_vencimiento")            public String fechaDeVencimiento = "";
    @JsonProperty("moneda")                          public int moneda = 1; // 1=Soles
    @JsonProperty("tipo_de_cambio")                  public String tipoDeCambio = "";
    @JsonProperty("porcentaje_de_igv")               public BigDecimal porcentajeDeIgv = new BigDecimal("18.00");
    @JsonProperty("total_gravada")                   public BigDecimal totalGravada;
    @JsonProperty("total_inafecta")                  public String totalInafecta = "";
    @JsonProperty("total_exonerada")                 public String totalExonerada = "";
    @JsonProperty("total_igv")                       public BigDecimal totalIgv;
    @JsonProperty("total")                           public BigDecimal total;
    @JsonProperty("detraccion")                      public boolean detraccion = false;
    @JsonProperty("observaciones")                   public String observaciones = "";
    @JsonProperty("condiciones_de_pago")             public String condicionesDePago = "CONTADO";
    @JsonProperty("medio_de_pago")                   public String medioDePago = "EFECTIVO";
    @JsonProperty("enviar_automaticamente_a_la_sunat") public boolean enviarSunat = true;
    @JsonProperty("enviar_automaticamente_al_cliente") public boolean enviarCliente = false;
    @JsonProperty("formato_de_pdf")                  public String formatoDePdf = "";
    @JsonProperty("items")                           public List<NubefactItemDTO> items;
}
