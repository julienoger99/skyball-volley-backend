package com.skyball.volley.auth.controller;

import com.skyball.volley.auth.domain.EmailVerificationToken;
import com.skyball.volley.auth.dto.ResendVerificationDto;
import com.skyball.volley.auth.persistence.EmailVerificationTokenRepository;
import com.skyball.volley.common.AbstractIT;
import com.skyball.volley.user.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EmailVerificationIT extends AbstractIT {

    @Autowired private EmailVerificationTokenRepository tokenRepository;

    private EmailVerificationToken savedToken(AppUser user, LocalDateTime expiresAt, boolean used) {
        return tokenRepository.save(EmailVerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(expiresAt)
                .used(used)
                .build());
    }

    // ── verify ────────────────────────────────────────────────────────────────

    @Test
    void verify_validToken_returns200AndMarksUserVerified() throws Exception {
        AppUser user = savedUnverifiedUser("toVerify");
        EmailVerificationToken token = savedToken(user, LocalDateTime.now().plusHours(24), false);

        mvc.perform(get("/api/v1/auth/verify").param("token", token.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully. You can now log in."));

        assertThat(userRepository.findById(user.getId()).orElseThrow().isEmailVerified()).isTrue();
    }

    @Test
    void verify_unknownToken_returns400() throws Exception {
        mvc.perform(get("/api/v1/auth/verify").param("token", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid verification token"));
    }

    @Test
    void verify_expiredToken_returns400() throws Exception {
        AppUser user = savedUnverifiedUser("expiredUser");
        EmailVerificationToken token = savedToken(user, LocalDateTime.now().minusHours(1), false);

        mvc.perform(get("/api/v1/auth/verify").param("token", token.getToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Verification link has expired — please request a new one"));
    }

    @Test
    void verify_alreadyUsedToken_returns400() throws Exception {
        AppUser user = savedUnverifiedUser("usedTokenUser");
        EmailVerificationToken token = savedToken(user, LocalDateTime.now().plusHours(24), true);

        mvc.perform(get("/api/v1/auth/verify").param("token", token.getToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This verification link has already been used"));
    }

    // ── resend-verification ───────────────────────────────────────────────────

    @Test
    void resend_knownUnverifiedEmail_returns200() throws Exception {
        savedUnverifiedUser("resendUser");

        var dto = new ResendVerificationDto();
        dto.setEmail("resendUser@test.com");

        mvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification email sent. Please check your inbox."));
    }

    @Test
    void resend_unknownEmail_returns404() throws Exception {
        var dto = new ResendVerificationDto();
        dto.setEmail("nobody@test.com");

        mvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: nobody@test.com"));
    }

    @Test
    void resend_alreadyVerifiedEmail_returns400() throws Exception {
        savedUser("alreadyVerified", false);

        var dto = new ResendVerificationDto();
        dto.setEmail("alreadyVerified@test.com");

        mvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This account is already verified"));
    }

    @Test
    void resend_invalidEmail_returns400() throws Exception {
        mvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }
}
