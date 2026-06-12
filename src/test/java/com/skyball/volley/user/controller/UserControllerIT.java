package com.skyball.volley.user.controller;

import com.skyball.volley.common.AbstractIT;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.dto.UpdateUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIT extends AbstractIT {

    private AppUser alice;
    private AppUser bob;

    @BeforeEach
    void setUp() {
        alice = savedUser("alice", false);
        bob = savedUser("bob", false);
    }

    // ── GET /api/v1/users/me ─────────────────────────────────────────────────────

    @Test
    void getMe_authenticated_returns200() throws Exception {
        mvc.perform(get("/api/v1/users/me").with(asUser("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void getMe_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/v1/users ────────────────────────────────────────────────────────

    @Test
    void getAllUsers_authenticated_returns200() throws Exception {
        mvc.perform(get("/api/v1/users").with(asUser("alice")))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/v1/users/{id} ───────────────────────────────────────────────────

    @Test
    void getUserById_found_returns200() throws Exception {
        mvc.perform(get("/api/v1/users/" + alice.getId()).with(asUser("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        mvc.perform(get("/api/v1/users/99999").with(asUser("alice")))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/v1/users/{id} ───────────────────────────────────────────────────

    @Test
    void updateUser_self_returns200() throws Exception {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setUsername("alice_updated");

        mvc.perform(put("/api/v1/users/" + alice.getId())
                        .with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice_updated"));
    }

    @Test
    void updateUser_otherUser_returns403() throws Exception {
        UpdateUserDto dto = new UpdateUserDto();
        dto.setUsername("hacked");

        mvc.perform(put("/api/v1/users/" + bob.getId())
                        .with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/v1/users/{id} ────────────────────────────────────────────────

    @Test
    void deleteUser_self_returns204() throws Exception {
        mvc.perform(delete("/api/v1/users/" + alice.getId()).with(asUser("alice")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_otherUser_returns403() throws Exception {
        mvc.perform(delete("/api/v1/users/" + bob.getId()).with(asUser("alice")))
                .andExpect(status().isForbidden());
    }
}
