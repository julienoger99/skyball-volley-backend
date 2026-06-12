package com.skyball.volley.match.controller;

import com.skyball.volley.championship.domain.Championship;
import com.skyball.volley.championship.persistence.ChampionshipRepository;
import com.skyball.volley.common.AbstractIT;
import com.skyball.volley.common.MembershipRole;
import com.skyball.volley.match.domain.Match;
import com.skyball.volley.match.domain.MatchSet;
import com.skyball.volley.match.domain.AttendanceStatus;
import com.skyball.volley.match.dto.BulkMatchSetsDto;
import com.skyball.volley.match.dto.CreateMatchDto;
import com.skyball.volley.match.dto.CreateMatchSetDto;
import com.skyball.volley.match.dto.UpdateAttendanceDto;
import com.skyball.volley.match.dto.UpdateMatchDto;
import com.skyball.volley.match.domain.MatchPlayer;
import com.skyball.volley.match.domain.MatchPlayerId;
import com.skyball.volley.match.persistence.MatchPlayerRepository;
import com.skyball.volley.match.persistence.MatchRepository;
import com.skyball.volley.match.persistence.MatchSetRepository;
import com.skyball.volley.team.domain.Team;
import com.skyball.volley.team.domain.TeamCategory;
import com.skyball.volley.team.domain.TeamGender;
import com.skyball.volley.team.domain.TeamMembership;
import com.skyball.volley.team.domain.TeamMembershipId;
import com.skyball.volley.team.persistence.TeamMembershipRepository;
import com.skyball.volley.team.persistence.TeamRepository;
import com.skyball.volley.user.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MatchControllerIT extends AbstractIT {

    @Autowired private MatchRepository matchRepository;
    @Autowired private MatchSetRepository matchSetRepository;
    @Autowired private MatchPlayerRepository matchPlayerRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMembershipRepository teamMembershipRepository;
    @Autowired private ChampionshipRepository championshipRepository;

    private AppUser player;
    private AppUser manager;
    private AppUser admin;
    private Team team;
    private Match match;

    @BeforeEach
    void setUp() {
        player = savedUser("player", false);
        manager = savedUser("manager", false);
        admin = savedUser("admin_user", false);
        savedUser("outsider", false);
        savedUser("superadmin", true);

        team = teamRepository.save(Team.builder()
                .name("Test Team").category(TeamCategory.SENIOR).gender(TeamGender.MALE).build());

        addToTeam(player, team, MembershipRole.MEMBER);
        addToTeam(manager, team, MembershipRole.MANAGER);
        addToTeam(admin, team, MembershipRole.ADMIN);

        match = matchRepository.save(Match.builder()
                .team(team).opponentName("Adversaire")
                .matchDate(LocalDateTime.now().plusDays(7)).home(true)
                .build());
    }

    @AfterEach
    void cleanUpMatches() {
        matchRepository.deleteAll();
        teamMembershipRepository.deleteAll();
        teamRepository.deleteAll();
        championshipRepository.deleteAll();
    }

    private void addToTeam(AppUser user, Team t, MembershipRole role) {
        teamMembershipRepository.save(TeamMembership.builder()
                .id(new TeamMembershipId(user.getId(), t.getId()))
                .user(user).team(t).role(role).build());
    }

    // ── GET /api/teams/{teamId}/matches ───────────────────────────────────────

    @Test
    void getMatchesByTeam_teamMember_returns200() throws Exception {
        mvc.perform(get("/api/v1/teams/" + team.getId() + "/matches").with(asUser("player")))
                .andExpect(status().isOk());
    }

    @Test
    void getMatchesByTeam_nonMember_returns403() throws Exception {
        mvc.perform(get("/api/v1/teams/" + team.getId() + "/matches").with(asUser("outsider")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMatchesByTeam_teamNotFound_returns404() throws Exception {
        mvc.perform(get("/api/v1/teams/99999/matches").with(asUser("superadmin")))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/championships/{championshipId}/matches ───────────────────────

    @Test
    void getMatchesByChampionship_found_returns200() throws Exception {
        Championship champ = championshipRepository.save(
                Championship.builder().name("Championnat Régional").season("2025-2026").build());
        matchRepository.save(Match.builder()
                .team(team).opponentName("Adversaire")
                .matchDate(LocalDateTime.now().plusDays(14)).home(false)
                .championship(champ).build());

        mvc.perform(get("/api/v1/championships/" + champ.getId() + "/matches").with(asUser("player")))
                .andExpect(status().isOk());
    }

    @Test
    void getMatchesByChampionship_notFound_returns404() throws Exception {
        mvc.perform(get("/api/v1/championships/99999/matches").with(asUser("player")))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/teams/{teamId}/matches ──────────────────────────────────────

    @Test
    void createMatch_teamManager_returns201() throws Exception {
        var dto = new CreateMatchDto();
        dto.setOpponentName("VC Paris");
        dto.setMatchDate(LocalDateTime.now().plusDays(30));
        dto.setHome(true);

        mvc.perform(post("/api/v1/teams/" + team.getId() + "/matches")
                        .with(asUser("manager"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.team.id").value(team.getId()));
    }

    @Test
    void createMatch_teamMember_returns403() throws Exception {
        var dto = new CreateMatchDto();
        dto.setOpponentName("VC Paris");
        dto.setMatchDate(LocalDateTime.now().plusDays(30));

        mvc.perform(post("/api/v1/teams/" + team.getId() + "/matches")
                        .with(asUser("player"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/matches/{id} ─────────────────────────────────────────────────

    @Test
    void getMatchById_teamMember_returns200() throws Exception {
        mvc.perform(get("/api/v1/matches/" + match.getId()).with(asUser("player")))
                .andExpect(status().isOk());
    }

    @Test
    void getMatchById_nonMember_returns403() throws Exception {
        mvc.perform(get("/api/v1/matches/" + match.getId()).with(asUser("outsider")))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/matches/{id} ─────────────────────────────────────────────────

    @Test
    void updateMatch_teamManager_returns200() throws Exception {
        var dto = new UpdateMatchDto();
        dto.setLocation("New Gym");

        mvc.perform(put("/api/v1/matches/" + match.getId())
                        .with(asUser("manager"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("New Gym"));
    }

    @Test
    void updateMatch_teamMember_returns403() throws Exception {
        var dto = new UpdateMatchDto();
        dto.setLocation("Hacked");

        mvc.perform(put("/api/v1/matches/" + match.getId())
                        .with(asUser("player"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/matches/{id} ──────────────────────────────────────────────

    @Test
    void deleteMatch_teamAdmin_returns204() throws Exception {
        mvc.perform(delete("/api/v1/matches/" + match.getId()).with(asUser("admin_user")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMatch_teamManager_returns403() throws Exception {
        mvc.perform(delete("/api/v1/matches/" + match.getId()).with(asUser("manager")))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/matches/{matchId}/players/{userId}/attendance ─────────────────

    @Test
    void updateAttendance_self_returns200() throws Exception {
        var dto = new UpdateAttendanceDto();
        dto.setAttendanceStatus(AttendanceStatus.PRESENT);

        mvc.perform(put("/api/v1/matches/" + match.getId() + "/players/" + player.getId() + "/attendance")
                        .with(asUser("player"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateAttendance_teamManager_returns200() throws Exception {
        var dto = new UpdateAttendanceDto();
        dto.setAttendanceStatus(AttendanceStatus.ABSENT);

        mvc.perform(put("/api/v1/matches/" + match.getId() + "/players/" + player.getId() + "/attendance")
                        .with(asUser("manager"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateAttendance_unrelated_returns403() throws Exception {
        var dto = new UpdateAttendanceDto();
        dto.setAttendanceStatus(AttendanceStatus.PRESENT);

        mvc.perform(put("/api/v1/matches/" + match.getId() + "/players/" + player.getId() + "/attendance")
                        .with(asUser("outsider"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/matches/{matchId}/sets ───────────────────────────────────────

    @Test
    void addOrUpdateSet_teamManager_returns200() throws Exception {
        var dto = new CreateMatchSetDto();
        dto.setSetNumber(1);
        dto.setTeamPoints(25);
        dto.setOpponentPoints(18);

        mvc.perform(put("/api/v1/matches/" + match.getId() + "/sets")
                        .with(asUser("manager"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets[0].setNumber").value(1));
    }

    @Test
    void addOrUpdateSet_teamMember_returns403() throws Exception {
        var dto = new CreateMatchSetDto();
        dto.setSetNumber(1);
        dto.setTeamPoints(25);
        dto.setOpponentPoints(18);

        mvc.perform(put("/api/v1/matches/" + match.getId() + "/sets")
                        .with(asUser("player"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/matches/{matchId}/sets/bulk ──────────────────────────────────

    @Test
    void setAllSets_teamManager_returns200() throws Exception {
        var setDto = new CreateMatchSetDto();
        setDto.setSetNumber(1);
        setDto.setTeamPoints(25);
        setDto.setOpponentPoints(18);

        var dto = new BulkMatchSetsDto();
        dto.setSets(List.of(setDto));

        mvc.perform(put("/api/v1/matches/" + match.getId() + "/sets/bulk")
                        .with(asUser("manager"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets[0].teamPoints").value(25));
    }

    @Test
    void setAllSets_teamMember_returns403() throws Exception {
        var setDto = new CreateMatchSetDto();
        setDto.setSetNumber(1);
        setDto.setTeamPoints(25);
        setDto.setOpponentPoints(18);

        var dto = new BulkMatchSetsDto();
        dto.setSets(List.of(setDto));

        mvc.perform(put("/api/v1/matches/" + match.getId() + "/sets/bulk")
                        .with(asUser("player"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/matches/{matchId}/sets/{setNumber} ────────────────────────

    @Test
    void deleteSet_teamManager_returns200() throws Exception {
        matchSetRepository.save(MatchSet.builder()
                .match(match).setNumber(1).teamPoints(25).opponentPoints(18).build());

        mvc.perform(delete("/api/v1/matches/" + match.getId() + "/sets/1")
                        .with(asUser("manager")))
                .andExpect(status().isOk());
    }

    @Test
    void deleteSet_teamMember_returns403() throws Exception {
        mvc.perform(delete("/api/v1/matches/" + match.getId() + "/sets/1")
                        .with(asUser("player")))
                .andExpect(status().isForbidden());
    }

    // ── PUT /api/matches/{matchId}/captain/{userId} ───────────────────────────

    @Test
    void setCaptain_teamManager_returns200() throws Exception {
        matchPlayerRepository.save(MatchPlayer.builder()
                .id(new MatchPlayerId(match.getId(), player.getId()))
                .match(match).player(player).build());

        mvc.perform(put("/api/v1/matches/" + match.getId() + "/captain/" + player.getId())
                        .with(asUser("manager")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players[0].captain").value(true));
    }

    @Test
    void setCaptain_teamMember_returns403() throws Exception {
        mvc.perform(put("/api/v1/matches/" + match.getId() + "/captain/" + player.getId())
                        .with(asUser("player")))
                .andExpect(status().isForbidden());
    }

    @Test
    void setCaptain_playerNotInMatch_returns404() throws Exception {
        mvc.perform(put("/api/v1/matches/" + match.getId() + "/captain/" + player.getId())
                        .with(asUser("manager")))
                .andExpect(status().isNotFound());
    }
}
