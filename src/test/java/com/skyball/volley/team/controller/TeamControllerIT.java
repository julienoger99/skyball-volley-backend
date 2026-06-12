package com.skyball.volley.team.controller;

import com.skyball.volley.club.domain.Club;
import com.skyball.volley.club.domain.ClubMembershipId;
import com.skyball.volley.common.AbstractIT;
import com.skyball.volley.common.MembershipRole;
import com.skyball.volley.common.UpdateMemberRoleDto;
import com.skyball.volley.team.domain.Team;
import com.skyball.volley.team.domain.TeamCategory;
import com.skyball.volley.team.domain.TeamGender;
import com.skyball.volley.team.domain.TeamMembership;
import com.skyball.volley.team.domain.TeamMembershipId;
import com.skyball.volley.team.dto.CreateTeamDto;
import com.skyball.volley.team.dto.UpdateTeamDto;
import com.skyball.volley.team.persistence.TeamMembershipRepository;
import com.skyball.volley.team.persistence.TeamRepository;
import com.skyball.volley.user.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TeamControllerIT extends AbstractIT {

    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMembershipRepository teamMembershipRepository;

    private AppUser regular;
    private AppUser superadmin;
    private Club club;

    @BeforeEach
    void setUp() {
        regular = savedUser("regular", false);
        superadmin = savedUser("superadmin", true);
        club = savedClub("Club Test");
    }

    @AfterEach
    void cleanUpTeams() {
        teamMembershipRepository.deleteAll();
        teamRepository.deleteAll();
    }

    private Team savedTeam(String name, Club c) {
        return teamRepository.save(Team.builder()
                .name(name).category(TeamCategory.SENIOR).gender(TeamGender.MALE).club(c).build());
    }

    private void addToTeam(AppUser user, Team team, MembershipRole role) {
        teamMembershipRepository.save(TeamMembership.builder()
                .id(new TeamMembershipId(user.getId(), team.getId()))
                .user(user).team(team).role(role).build());
    }

    // ── GET /api/v1/teams ────────────────────────────────────────────────────────

    @Test
    void getAllTeams_authenticated_returns200() throws Exception {
        mvc.perform(get("/api/v1/teams").with(asUser("regular")))
                .andExpect(status().isOk());
    }

    @Test
    void getAllTeams_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/v1/teams"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/v1/teams/club/{clubId} ──────────────────────────────────────────

    @Test
    void getTeamsByClub_clubNotFound_returns404() throws Exception {
        mvc.perform(get("/api/v1/teams/club/99999").with(asUser("regular")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTeamsByClub_found_returns200() throws Exception {
        savedTeam("Team A", club);

        mvc.perform(get("/api/v1/teams/club/" + club.getId()).with(asUser("regular")))
                .andExpect(status().isOk());
    }

    // ── GET /api/v1/teams/{id} ───────────────────────────────────────────────────

    @Test
    void getTeamById_found_returns200() throws Exception {
        Team team = savedTeam("Team A", null);

        mvc.perform(get("/api/v1/teams/" + team.getId()).with(asUser("regular")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Team A"));
    }

    @Test
    void getTeamById_notFound_returns404() throws Exception {
        mvc.perform(get("/api/v1/teams/99999").with(asUser("regular")))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/v1/teams ───────────────────────────────────────────────────────

    @Test
    void createTeam_independentTeam_returns201() throws Exception {
        var dto = new CreateTeamDto();
        dto.setName("My Team");
        dto.setCategory(TeamCategory.SENIOR);
        dto.setGender(TeamGender.MALE);

        mvc.perform(post("/api/v1/teams")
                        .with(asUser("regular"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Team"));
    }

    @Test
    void createTeam_withClub_clubAdmin_returns201() throws Exception {
        addToClub(regular, club, MembershipRole.ADMIN);

        var dto = new CreateTeamDto();
        dto.setName("Club Team");
        dto.setCategory(TeamCategory.SENIOR);
        dto.setGender(TeamGender.MALE);
        dto.setClubId(club.getId());

        mvc.perform(post("/api/v1/teams")
                        .with(asUser("regular"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createTeam_withClub_notClubAdmin_returns403() throws Exception {
        var dto = new CreateTeamDto();
        dto.setName("Club Team");
        dto.setCategory(TeamCategory.SENIOR);
        dto.setGender(TeamGender.MALE);
        dto.setClubId(club.getId());

        mvc.perform(post("/api/v1/teams")
                        .with(asUser("regular"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/v1/teams/{id} ───────────────────────────────────────────────────

    @Test
    void updateTeam_manager_returns200() throws Exception {
        Team team = savedTeam("Old Name", null);
        addToTeam(regular, team, MembershipRole.MANAGER);

        var dto = new UpdateTeamDto();
        dto.setName("New Name");

        mvc.perform(put("/api/v1/teams/" + team.getId())
                        .with(asUser("regular"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void updateTeam_member_returns403() throws Exception {
        Team team = savedTeam("Team", null);
        addToTeam(regular, team, MembershipRole.MEMBER);

        var dto = new UpdateTeamDto();
        dto.setName("Hacked");

        mvc.perform(put("/api/v1/teams/" + team.getId())
                        .with(asUser("regular"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/v1/teams/{id} ────────────────────────────────────────────────

    @Test
    void deleteTeam_admin_returns204() throws Exception {
        Team team = savedTeam("ToDelete", null);
        addToTeam(regular, team, MembershipRole.ADMIN);

        mvc.perform(delete("/api/v1/teams/" + team.getId()).with(asUser("regular")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTeam_manager_returns403() throws Exception {
        Team team = savedTeam("Protected", null);
        addToTeam(regular, team, MembershipRole.MANAGER);

        mvc.perform(delete("/api/v1/teams/" + team.getId()).with(asUser("regular")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTeam_notFound_returns404() throws Exception {
        mvc.perform(delete("/api/v1/teams/99999").with(asUser("superadmin")))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/v1/teams/{teamId}/members/{userId} ─────────────────────────────

    @Test
    void addMember_manager_returns200() throws Exception {
        Team team = savedTeam("Team", null);
        addToTeam(regular, team, MembershipRole.MANAGER);

        mvc.perform(post("/api/v1/teams/" + team.getId() + "/members/" + superadmin.getId())
                        .with(asUser("regular")))
                .andExpect(status().isOk());
    }

    @Test
    void addMember_selfJoin_indieTeam_returns200() throws Exception {
        Team team = savedTeam("Indie Team", null);

        mvc.perform(post("/api/v1/teams/" + team.getId() + "/members/" + regular.getId())
                        .with(asUser("regular")))
                .andExpect(status().isOk());
    }

    @Test
    void addMember_selfJoin_ownClubTeam_returns200() throws Exception {
        addToClub(regular, club, MembershipRole.MEMBER);
        Team team = savedTeam("Club Team", club);

        mvc.perform(post("/api/v1/teams/" + team.getId() + "/members/" + regular.getId())
                        .with(asUser("regular")))
                .andExpect(status().isOk());
    }

    @Test
    void addMember_selfJoin_noClub_autoJoinsClub_returns200() throws Exception {
        Team team = savedTeam("Club Team", club);

        mvc.perform(post("/api/v1/teams/" + team.getId() + "/members/" + regular.getId())
                        .with(asUser("regular")))
                .andExpect(status().isOk());

        assertThat(clubMembershipRepository.existsById(new ClubMembershipId(regular.getId(), club.getId()))).isTrue();
    }

    @Test
    void addMember_selfJoin_differentClub_returns400() throws Exception {
        Club otherClub = savedClub("Other Club");
        addToClub(regular, otherClub, MembershipRole.MEMBER);
        Team team = savedTeam("Club Team", club);

        mvc.perform(post("/api/v1/teams/" + team.getId() + "/members/" + regular.getId())
                        .with(asUser("regular")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMember_nonSelf_notManager_returns403() throws Exception {
        Team team = savedTeam("Team", null);
        addToTeam(regular, team, MembershipRole.MEMBER);

        mvc.perform(post("/api/v1/teams/" + team.getId() + "/members/" + superadmin.getId())
                        .with(asUser("regular")))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/v1/teams/{teamId}/members/{userId} ───────────────────────────

    @Test
    void removeMember_selfLeave_returns200() throws Exception {
        Team team = savedTeam("Team", null);
        addToTeam(regular, team, MembershipRole.MEMBER);

        mvc.perform(delete("/api/v1/teams/" + team.getId() + "/members/" + regular.getId())
                        .with(asUser("regular")))
                .andExpect(status().isOk());
    }

    @Test
    void removeMember_manager_returns200() throws Exception {
        Team team = savedTeam("Team", null);
        addToTeam(regular, team, MembershipRole.MANAGER);
        addToTeam(superadmin, team, MembershipRole.MEMBER);

        mvc.perform(delete("/api/v1/teams/" + team.getId() + "/members/" + superadmin.getId())
                        .with(asUser("regular")))
                .andExpect(status().isOk());
    }

    // ── PATCH /api/v1/teams/{teamId}/members/{userId}/role ───────────────────────

    @Test
    void updateMemberRole_admin_returns200() throws Exception {
        Team team = savedTeam("Team", null);
        addToTeam(regular, team, MembershipRole.ADMIN);
        addToTeam(superadmin, team, MembershipRole.MEMBER);

        var dto = new UpdateMemberRoleDto();
        dto.setRole(MembershipRole.MANAGER);

        mvc.perform(patch("/api/v1/teams/" + team.getId() + "/members/" + superadmin.getId() + "/role")
                        .with(asUser("regular"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}
