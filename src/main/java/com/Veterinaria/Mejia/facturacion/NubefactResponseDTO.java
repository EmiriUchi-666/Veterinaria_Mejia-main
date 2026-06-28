package com.Veterinaria.Mejia.facturacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NubefactResponseDTO {
    @JsonProperty("codigo")           public String codigo;      // "0" = éxito
    @JsonProperty("sunat_estado")     public String sunatEstado;
    @JsonProperty("sunat_codigo")     public String sunatCodigo;
    @JsonProperty("sunat_descripcion") public String sunatDescripcion;
    @JsonProperty("sunat_nota")       public String sunatNota;
    @JsonProperty("codigo_hash")      public String codigoHash;
    @JsonProperty("serie")            public String serie;
    @JsonProperty("numero")           public int numero;
    @JsonProperty("enlace_del_pdf")   public String enlaceDelPdf;
    @JsonProperty("enlace_del_xml")   public String enlaceDelXml;
    @JsonProperty("enlace_del_cdr")   public String enlaceDelCdr;

    public boolean esExitoso() {
        return "0".equals(codigo) || "SUNAT_ACEPTADO".equalsIgnoreCase(sunatEstado);
    }
}
