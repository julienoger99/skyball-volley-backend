package com.skyball.volley.auth.controller;

import com.skyball.volley.auth.dto.LoginDto;
import com.skyball.volley.auth.dto.RegisterDto;
import com.skyball.volley.common.AbstractIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIT extends AbstractIT {

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_success_returns201() throws Exception {
        var dto = new RegisterDto();
        dto.setUsername("newuser");
        dto.setEmail("newuser@test.com");
        dto.setPassword("password123");

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful. Please check your email to verify your account."));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        savedUser("existing", false);

        var dto = new RegisterDto();
        dto.setUsername("existing");
        dto.setEmail("other@test.com");
        dto.setPassword("password");

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        savedUser("alice", false);

        var dto = new RegisterDto();
        dto.setUsername("bob");
        dto.setEmail("alice@test.com");
        dto.setPassword("password");

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_missingFields_returns400() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_emailNotVerified_returns403() throws Exception {
        savedUnverifiedUser("unverified");

        var dto = new LoginDto();
        dto.setUsername("unverified");
        dto.setPassword("password");

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Please verify your email address before logging in"));
    }

    @Test
    void login_success_returns200WithToken() throws Exception {
        savedUser("loginuser", false);

        var dto = new LoginDto();
        dto.setUsername("loginuser");
        dto.setPassword("password");

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("loginuser"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        savedUser("loginuser2", false);

        var dto = new LoginDto();
        dto.setUsername("loginuser2");
        dto.setPassword("wrongpassword");

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownUser_returns401() throws Exception {
        var dto = new LoginDto();
        dto.setUsername("nobody");
        dto.setPassword("password");

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    void logout_authenticated_returns204() throws Exception {
        mvc.perform(post("/api/v1/auth/logout")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(j -> j
                                        .jti("test-jti-logout")
                                        .subject("logoutuser")
                                        .issuedAt(Instant.now())
                                        .expiresAt(Instant.now().plusSeconds(3600)))))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_noToken_returns204() throws Exception {
        mvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());
    }

    // ── protection ────────────────────────────────────────────────────────────

    @Test
    void protectedEndpoint_noToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/clubs"))
                .andExpect(status().isUnauthorized());
    }
}
