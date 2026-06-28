package com.Veterinaria.Mejia.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * M3 — Documentación automática de API con SpringDoc OpenAPI.
 * Disponible en: /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("VetMejía API")
                .description("Sistema de Gestión Veterinaria — API REST interna y de integración con SUNAT/Nubefact.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Veterinaria Mejía")
                    .email("sistemas@vetmejia.pe"))
                .license(new License().name("Uso interno").url("https://vetmejia.pe")))
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
            .components(new Components()
                .addSecuritySchemes("basicAuth",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")));
    }
}
