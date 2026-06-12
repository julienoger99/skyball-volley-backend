package com.skyball.volley.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.api.base-path}")
    private String apiBasePath;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path(apiBasePath + "/auth/reset-password")
                .queryParam("token", token)
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de votre mot de passe - Skyball Volley");
        message.setText("""
                Vous avez demandé à réinitialiser votre mot de passe.
                
                Cliquez sur le lien ci-dessous (valable 1 heure) :
                %s
                
                Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.
                """.formatted(resetUrl));

        mailSender.send(message);
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path(apiBasePath + "/auth/verify")
                .queryParam("token", token)
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Vérifiez votre adresse email - Skyball Volley");
        message.setText("""
                Bienvenue sur Skyball Volley !
                
                Cliquez sur le lien ci-dessous pour vérifier votre adresse email :
                %s
                
                Ce lien expire dans 24 heures.
                
                Si vous n'êtes pas à l'origine de cette inscription, ignorez ce message.
                """.formatted(verifyUrl));

        mailSender.send(message);
    }
}
