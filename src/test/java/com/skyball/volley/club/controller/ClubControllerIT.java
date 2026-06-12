package com.skyball.volley.club.controller;

import com.skyball.volley.club.domain.Club;
import com.skyball.volley.club.dto.CreateClubDto;
import com.skyball.volley.club.dto.UpdateClubDto;
import com.skyball.volley.common.AbstractIT;
import com.skyball.volley.common.MembershipRole;
import com.skyball.volley.common.UpdateMemberRoleDto;
import com.skyball.volley.user.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClubControllerIT extends AbstractIT {

    private AppUser admin;
    private AppUser member;
    private AppUser outsider;
    private Club club;

    @BeforeEach
    void setUp() {
        admin = savedUser("admin", false);
        member = savedUser("member", false);
        outsider = savedUser("outsider", false);

        club = savedClub("Test Club");
        addToClub(admin, club, MembershipRole.ADMIN);
        addToClub(member, club, MembershipRole.MEMBER);
    }

    // ── GET /api/v1/clubs ────────────────────────────────────────────────────────

    @Test
    void getAllClubs_authenticated_returns200() throws Exception {
        mvc.perform(get("/api/v1/clubs").with(asUser("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Club"));
    }

    @Test
    void getAllClubs_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/v1/clubs"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/v1/clubs/{id} ───────────────────────────────────────────────────

    @Test
    void getClubById_exists_returns200() throws Exception {
        mvc.perform(get("/api/v1/clubs/" + club.getId()).with(asUser("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(club.getId()));
    }

    @Test
    void getClubById_notFound_returns404() throws Exception {
        mvc.perform(get("/api/v1/clubs/99999").with(asUser("admin")))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/v1/clubs ───────────────────────────────────────────────────────

    @Test
    void createClub_freeUser_returns201() throws Exception {
        var dto = new CreateClubDto();
        dto.setName("New Club");
        dto.setCity("Lyon");

        mvc.perform(post("/api/v1/clubs")
                        .with(asUser("outsider"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Club"));
    }

    @Test
    void createClub_userAlreadyInClub_returns409() throws Exception {
        var dto = new CreateClubDto();
        dto.setName("Another Club");
        dto.setCity("Lyon");

        mvc.perform(post("/api/v1/clubs")
                        .with(asUser("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createClub_duplicateName_returns409() throws Exception {
        var dto = new CreateClubDto();
        dto.setName("Test Club");
        dto.setCity("Lyon");

        mvc.perform(post("/api/v1/clubs")
                        .with(asUser("outsider"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // ── PUT /api/v1/clubs/{id} ───────────────────────────────────────────────────

    @Test
    void updateClub_clubAdmin_returns200() throws Exception {
        var dto = new UpdateClubDto();
        dto.setCity("Marseille");

        mvc.perform(put("/api/v1/clubs/" + club.getId())
                        .with(asUser("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Marseille"));
    }

    @Test
    void updateClub_clubMember_returns403() throws Exception {
        var dto = new UpdateClubDto();
        dto.setCity("Marseille");

        mvc.perform(put("/api/v1/clubs/" + club.getId())
                        .with(asUser("member"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateClub_outsider_returns403() throws Exception {
        var dto = new UpdateClubDto();
        dto.setCity("Marseille");

        mvc.perform(put("/api/v1/clubs/" + club.getId())
                        .with(asUser("outsider"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/v1/clubs/{id} ────────────────────────────────────────────────

    @Test
    void deleteClub_clubAdmin_returns204() throws Exception {
        mvc.perform(delete("/api/v1/clubs/" + club.getId())
                        .with(asUser("admin")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteClub_clubMember_returns403() throws Exception {
        mvc.perform(delete("/api/v1/clubs/" + club.getId())
                        .with(asUser("member")))
                .andExpect(status().isForbidden());
    }

    // ── POST /api/v1/clubs/{clubId}/members/{userId} ─────────────────────────────

    @Test
    void joinClub_selfJoin_returns200() throws Exception {
        mvc.perform(post("/api/v1/clubs/" + club.getId() + "/members/" + outsider.getId())
                        .with(asUser("outsider")))
                .andExpect(status().isOk());
    }

    @Test
    void joinClub_joinForSomeoneElse_returns403() throws Exception {
        mvc.perform(post("/api/v1/clubs/" + club.getId() + "/members/" + outsider.getId())
                        .with(asUser("member")))
                .andExpect(status().isForbidden());
    }

    @Test
    void joinClub_alreadyInClub_returns409() throws Exception {
        mvc.perform(post("/api/v1/clubs/" + club.getId() + "/members/" + member.getId())
                        .with(asUser("member")))
                .andExpect(status().isConflict());
    }

    // ── PATCH /api/v1/clubs/{clubId}/members/{userId}/role ───────────────────────

    @Test
    void updateMemberRole_clubAdmin_returns200() throws Exception {
        var dto = new UpdateMemberRoleDto();
        dto.setRole(MembershipRole.MANAGER);

        mvc.perform(patch("/api/v1/clubs/" + club.getId() + "/members/" + member.getId() + "/role")
                        .with(asUser("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateMemberRole_clubMember_returns403() throws Exception {
        var dto = new UpdateMemberRoleDto();
        dto.setRole(MembershipRole.ADMIN);

        mvc.perform(patch("/api/v1/clubs/" + club.getId() + "/members/" + member.getId() + "/role")
                        .with(asUser("member"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/v1/clubs/{clubId}/members/{userId} ───────────────────────────

    @Test
    void leaveClub_selfLeave_returns204() throws Exception {
        mvc.perform(delete("/api/v1/clubs/" + club.getId() + "/members/" + member.getId())
                        .with(asUser("member")))
                .andExpect(status().isNoContent());
    }

    @Test
    void leaveClub_adminKicksUser_returns204() throws Exception {
        mvc.perform(delete("/api/v1/clubs/" + club.getId() + "/members/" + member.getId())
                        .with(asUser("admin")))
                .andExpect(status().isNoContent());
    }

    @Test
    void leaveClub_outsiderTriesToKick_returns403() throws Exception {
        mvc.perform(delete("/api/v1/clubs/" + club.getId() + "/members/" + member.getId())
                        .with(asUser("outsider")))
                .andExpect(status().isForbidden());
    }
}
