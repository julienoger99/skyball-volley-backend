package com.skyball.volley.championship.controller;

import com.skyball.volley.championship.domain.Championship;
import com.skyball.volley.championship.dto.CreateChampionshipDto;
import com.skyball.volley.championship.dto.UpdateChampionshipDto;
import com.skyball.volley.championship.persistence.ChampionshipRepository;
import com.skyball.volley.common.AbstractIT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChampionshipControllerIT extends AbstractIT {

    @Autowired private ChampionshipRepository championshipRepository;

    @BeforeEach
    void setUp() {
        savedUser("regular", false);
        savedUser("superadmin", true);
    }

    @AfterEach
    void cleanUpChampionships() {
        championshipRepository.deleteAll();
    }

    // ── GET /api/v1/championships ────────────────────────────────────────────────

    @Test
    void getAllChampionships_authenticated_returns200() throws Exception {
        mvc.perform(get("/api/v1/championships").with(asUser("regular")))
                .andExpect(status().isOk());
    }

    @Test
    void getAllChampionships_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/v1/championships"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/v1/championships/{id} ───────────────────────────────────────────

    @Test
    void getChampionshipById_found_returns200() throws Exception {
        Championship champ = championshipRepository.save(
                Championship.builder().name("Champ Régional").season("2025-2026").build());

        mvc.perform(get("/api/v1/championships/" + champ.getId()).with(asUser("regular")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Champ Régional"));
    }

    @Test
    void getChampionshipById_notFound_returns404() throws Exception {
        mvc.perform(get("/api/v1/championships/99999").with(asUser("regular")))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/v1/championships ───────────────────────────────────────────────

    @Test
    void createChampionship_superAdmin_returns201() throws Exception {
        var dto = new CreateChampionshipDto();
        dto.setName("Champ Régional");
        dto.setSeason("2025-2026");

        mvc.perform(post("/api/v1/championships")
                        .with(asUser("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Champ Régional"));
    }

    @Test
    void createChampionship_regularUser_returns403() throws Exception {
        var dto = new CreateChampionshipDto();
        dto.setName("Champ");
        dto.setSeason("2025-2026");

        mvc.perform(post("/api/v1/championships")
                        .with(asUser("regular"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createChampionship_invalidSeason_returns400() throws Exception {
        var dto = new CreateChampionshipDto();
        dto.setName("Champ");
        dto.setSeason("2025/2026");

        mvc.perform(post("/api/v1/championships")
                        .with(asUser("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/v1/championships/{id} ───────────────────────────────────────────

    @Test
    void updateChampionship_superAdmin_returns200() throws Exception {
        Championship champ = championshipRepository.save(
                Championship.builder().name("Old Name").season("2024-2025").build());

        var dto = new UpdateChampionshipDto();
        dto.setName("New Name");

        mvc.perform(put("/api/v1/championships/" + champ.getId())
                        .with(asUser("superadmin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void updateChampionship_regularUser_returns403() throws Exception {
        Championship champ = championshipRepository.save(
                Championship.builder().name("Champ").season("2024-2025").build());

        var dto = new UpdateChampionshipDto();
        dto.setName("Hacked");

        mvc.perform(put("/api/v1/championships/" + champ.getId())
                        .with(asUser("regular"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/v1/championships/{id} ────────────────────────────────────────

    @Test
    void deleteChampionship_superAdmin_returns204() throws Exception {
        Championship champ = championshipRepository.save(
                Championship.builder().name("To Delete").season("2024-2025").build());

        mvc.perform(delete("/api/v1/championships/" + champ.getId())
                        .with(asUser("superadmin")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteChampionship_regularUser_returns403() throws Exception {
        Championship champ = championshipRepository.save(
                Championship.builder().name("Protected").season("2024-2025").build());

        mvc.perform(delete("/api/v1/championships/" + champ.getId())
                        .with(asUser("regular")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteChampionship_notFound_returns404() throws Exception {
        mvc.perform(delete("/api/v1/championships/99999")
                        .with(asUser("superadmin")))
                .andExpect(status().isNotFound());
    }
}
