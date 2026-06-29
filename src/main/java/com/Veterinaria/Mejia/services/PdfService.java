package com.Veterinaria.Mejia.services;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * FASE 9: Servicio para la generación de documentos PDF a partir de plantillas Thymeleaf.
 * Utiliza la librería iText 7 con su addon html2pdf.
 */
@Service
@RequiredArgsConstructor
public class PdfService {

    private final TemplateEngine templateEngine;

    /**
     * Genera un archivo PDF como un array de bytes a partir de una plantilla Thymeleaf y un mapa de variables.
     *
     * @param templateName El nombre de la plantilla HTML (ej: "pdf/comprobante").
     * @param variables    Un mapa con los datos a inyectar en la plantilla.
     * @return Un array de bytes que representa el archivo PDF generado.
     */
    public byte[] generarPdfDesdeHtml(String templateName, Map<String, Object> variables) {
        // 1. Crear el contexto de Thymeleaf con las variables
        Context context = new Context();
        context.setVariables(variables);

        // 2. Procesar la plantilla HTML para obtener un String
        String html = templateEngine.process(templateName, context);

        // 3. Convertir el HTML a PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ConverterProperties converterProperties = new ConverterProperties();
        // Podríamos configurar la base URI para imágenes estáticas aquí si fuera necesario
        // converterProperties.setBaseUri("src/main/resources/static/images/");

        HtmlConverter.convertToPdf(html, outputStream, converterProperties);

        return outputStream.toByteArray();
    }
}
