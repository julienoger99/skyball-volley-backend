package com.skyball.volley.auth.controller;

import com.skyball.volley.auth.domain.PasswordResetToken;
import com.skyball.volley.auth.dto.ForgotPasswordDto;
import com.skyball.volley.auth.dto.ResetPasswordDto;
import com.skyball.volley.auth.persistence.PasswordResetTokenRepository;
import com.skyball.volley.common.AbstractIT;
import com.skyball.volley.user.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PasswordResetIT extends AbstractIT {

    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private PasswordResetToken savedToken(AppUser user, LocalDateTime expiresAt, boolean used) {
        return tokenRepository.save(PasswordResetToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(expiresAt)
                .used(used)
                .build());
    }

    // ── forgot-password ───────────────────────────────────────────────────────

    @Test
    void forgotPassword_knownEmail_returns200() throws Exception {
        savedUser("resetUser", false);

        var dto = new ForgotPasswordDto();
        dto.setEmail("resetUser@test.com");

        mvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void forgotPassword_unknownEmail_stillReturns200() throws Exception {
        var dto = new ForgotPasswordDto();
        dto.setEmail("nobody@test.com");

        mvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void forgotPassword_invalidEmail_returns400() throws Exception {
        mvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"not-an-email\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── reset-password ────────────────────────────────────────────────────────

    @Test
    void resetPassword_validToken_returns200AndUpdatesPassword() throws Exception {
        AppUser user = savedUser("pwdUser", false);
        PasswordResetToken token = savedToken(user, LocalDateTime.now().plusHours(1), false);

        var dto = new ResetPasswordDto();
        dto.setToken(token.getToken());
        dto.setNewPassword("newSecurePassword123");

        mvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully. You can now log in with your new password."));

        AppUser updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newSecurePassword123", updated.getPassword())).isTrue();
    }

    @Test
    void resetPassword_unknownToken_returns400() throws Exception {
        var dto = new ResetPasswordDto();
        dto.setToken("00000000-0000-0000-0000-000000000000");
        dto.setNewPassword("newPassword123");

        mvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid reset token"));
    }

    @Test
    void resetPassword_expiredToken_returns400() throws Exception {
        AppUser user = savedUser("expiredUser", false);
        PasswordResetToken token = savedToken(user, LocalDateTime.now().minusHours(1), false);

        var dto = new ResetPasswordDto();
        dto.setToken(token.getToken());
        dto.setNewPassword("newPassword123");

        mvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Reset link has expired — please request a new one"));
    }

    @Test
    void resetPassword_alreadyUsedToken_returns400() throws Exception {
        AppUser user = savedUser("usedUser", false);
        PasswordResetToken token = savedToken(user, LocalDateTime.now().plusHours(1), true);

        var dto = new ResetPasswordDto();
        dto.setToken(token.getToken());
        dto.setNewPassword("newPassword123");

        mvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This reset link has already been used"));
    }

    @Test
    void resetPassword_shortPassword_returns400() throws Exception {
        var dto = new ResetPasswordDto();
        dto.setToken("some-token");
        dto.setNewPassword("short");

        mvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
