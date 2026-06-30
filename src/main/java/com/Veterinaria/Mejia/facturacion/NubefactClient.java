package com.Veterinaria.Mejia.facturacion;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Cliente HTTP para comunicarse con la API de Nubefact.
 * Encapsula la lógica de construcción y envío de la petición.
 */
@Component("nubefactHttpClient")
public class NubefactClient {

    @Value("${nubefact.api.url}")
    private String apiUrl;

    @Value("${nubefact.api.token}")
    private String apiToken;

    @Value("${nubefact.ruc}")
    private String rucEmisor;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NubefactResponseDTO enviarComprobante(NubefactRequestDTO requestPayload) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(requestPayload);

        String url = apiUrl + "/" + rucEmisor + "/comprobante";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Token " + apiToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return objectMapper.readValue(response.body(), NubefactResponseDTO.class);
        } else {
            throw new RuntimeException("Error de Nubefact (HTTP " + response.statusCode() + "): " + response.body());
        }
    }
}