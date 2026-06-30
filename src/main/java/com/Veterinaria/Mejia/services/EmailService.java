package com.Veterinaria.Mejia.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Async // Para no bloquear el hilo principal mientras se envía el correo
    public void enviarEmailConAdjunto(String para, String asunto, String cuerpo, byte[] adjunto, String nombreAdjunto) {
        if (para == null || para.isBlank()) {
            log.warn("No se puede enviar correo: destinatario nulo o vacío.");
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(para);
            helper.setSubject(asunto);
            helper.setText(cuerpo, true); // true para interpretar como HTML
            helper.addAttachment(nombreAdjunto, new ByteArrayResource(adjunto));

            mailSender.send(message);
            log.info("Correo enviado exitosamente a: {}", para);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}: {}", para, e.getMessage());
        }
    }
}