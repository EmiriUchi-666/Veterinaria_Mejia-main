package com.Veterinaria.Mejia.clients;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Component("nubefactRestClient")
@RequiredArgsConstructor
public class NubefactClient {

    private final RestTemplate restTemplate;

    @Value("${nubefact.api.url}")
    private String apiUrl;

    @Value("${nubefact.api.token}")
    private String apiToken;

    public NubefactResponseDTO enviarComprobante(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        return restTemplate.postForObject(apiUrl, entity, NubefactResponseDTO.class);
    }

    @Data
    public static class NubefactResponseDTO {
        private String errors;

        @JsonProperty("enlace_del_pdf")
        private String enlaceDelPdf;

        @JsonProperty("enlace_del_xml")
        private String enlaceDelXml;

        @JsonProperty("codigo_hash")
        private String codigoHash;

        @JsonProperty("sunat_codigo_respuesta")
        private String sunatCodigo;

        @JsonProperty("sunat_descripcion")
        private String sunatDescripcion;

        public boolean esExitoso() {
            return errors == null || errors.trim().isEmpty();
        }
    }
}
