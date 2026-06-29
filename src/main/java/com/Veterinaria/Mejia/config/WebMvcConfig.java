package com.Veterinaria.Mejia.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${app.uploads.pacientes.path}")
    private String carpetaFotos;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String resourceLocation = "file:" + carpetaFotos;
        log.info("Mapeando /pacientes/** a la ruta física: {}", resourceLocation);

        registry.addResourceHandler("/pacientes/**")
                .addResourceLocations(resourceLocation);
    }
}