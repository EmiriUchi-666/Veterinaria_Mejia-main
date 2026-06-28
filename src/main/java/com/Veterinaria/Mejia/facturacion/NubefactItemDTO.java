package com.Veterinaria.Mejia.facturacion;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class NubefactItemDTO {
    @JsonProperty("unidad_de_medida")    public String unidadDeMedida = "ZZ";
    @JsonProperty("codigo")              public String codigo = "";
    @JsonProperty("descripcion")         public String descripcion;
    @JsonProperty("cantidad")            public BigDecimal cantidad;
    @JsonProperty("valor_unitario")      public BigDecimal valorUnitario;
    @JsonProperty("precio_unitario")     public BigDecimal precioUnitario;
    @JsonProperty("descuento")           public String descuento = "";
    @JsonProperty("subtotal")            public BigDecimal subtotal;
    @JsonProperty("tipo_de_igv")         public int tipoDeIgv = 1; // 1=Gravado-Onerosa
    @JsonProperty("igv")                 public BigDecimal igv;
    @JsonProperty("total")               public BigDecimal total;
    @JsonProperty("anticipo_regularizacion") public boolean anticipoRegularizacion = false;
    @JsonProperty("anticipo_documento_serie")  public String anticipoDocumentoSerie = "";
    @JsonProperty("anticipo_documento_numero") public String anticipoDocumentoNumero = "";
}
