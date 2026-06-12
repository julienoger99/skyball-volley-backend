package com.skyball.volley.match.service;

import com.skyball.volley.championship.domain.Championship;
import com.skyball.volley.championship.exception.ChampionshipNotFoundException;
import com.skyball.volley.championship.persistence.ChampionshipRepository;
import com.skyball.volley.match.exception.InvalidMatchConfigurationException;
import com.skyball.volley.match.domain.*;
import com.skyball.volley.match.dto.*;
import com.skyball.volley.match.exception.MatchNotFoundException;
import com.skyball.volley.match.persistence.MatchPlayerRepository;
import com.skyball.volley.match.persistence.MatchRepository;
import com.skyball.volley.match.persistence.MatchSetRepository;
import com.skyball.volley.team.domain.Team;
import com.skyball.volley.team.domain.TeamCategory;
import com.skyball.volley.team.domain.TeamGender;
import com.skyball.volley.team.exception.TeamNotFoundException;
import com.skyball.volley.team.persistence.TeamRepository;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.domain.Role;
import com.skyball.volley.user.exception.UserNotFoundException;
import com.skyball.volley.user.persistence.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private MatchSetRepository matchSetRepository;
    @Mock private MatchPlayerRepository matchPlayerRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private ChampionshipRepository championshipRepository;

    @InjectMocks private MatchService matchService;

    private static final Long TEAM_ID = 1L;
    private static final Long MATCH_ID = 100L;
    private static final Long USER_ID = 50L;

    private Team team() {
        return Team.builder()
                .id(TEAM_ID).name("Team A")
                .category(TeamCategory.SENIOR).gender(TeamGender.MALE)
                .build();
    }

    private Match match(Team team) {
        return Match.builder()
                .id(MATCH_ID).team(team).opponentName("Adversaire")
                .matchDate(LocalDateTime.now().plusDays(7)).home(true)
                .build();
    }

    private CreateMatchSetDto setDto(int number, int teamPts, int opponentPts) {
        CreateMatchSetDto dto = new CreateMatchSetDto();
        dto.setSetNumber(number);
        dto.setTeamPoints(teamPts);
        dto.setOpponentPoints(opponentPts);
        return dto;
    }

    // ── getMatchesByTeam ──────────────────────────────────────────────────────

    @Test
    void getMatchesByTeam_teamNotFound_throws() {
        when(teamRepository.existsById(TEAM_ID)).thenReturn(false);

        var pageable = Pageable.unpaged();
        assertThatThrownBy(() -> matchService.getMatchesByTeam(TEAM_ID, pageable))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void getMatchesByTeam_teamFound_returnsList() {
        Team team = team();
        when(teamRepository.existsById(TEAM_ID)).thenReturn(true);
        when(matchRepository.findByTeamId(eq(TEAM_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(match(team))));

        var result = matchService.getMatchesByTeam(TEAM_ID, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }

    // ── getMatchesByChampionship ──────────────────────────────────────────────

    @Test
    void getMatchesByChampionship_notFound_throws() {
        when(championshipRepository.findById(99L)).thenReturn(Optional.empty());

        var pageable = Pageable.unpaged();
        assertThatThrownBy(() -> matchService.getMatchesByChampionship(99L, pageable))
                .isInstanceOf(ChampionshipNotFoundException.class);
    }

    @Test
    void getMatchesByChampionship_found_returnsList() {
        Team team = team();
        when(championshipRepository.findById(1L)).thenReturn(Optional.of(Championship.builder().id(1L).name("C").season("2025-2026").build()));
        when(matchRepository.findByChampionshipId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(match(team))));

        var result = matchService.getMatchesByChampionship(1L, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }

    // ── createMatch ───────────────────────────────────────────────────────────

    @Test
    void createMatch_noOpponentProvided_throwsInvalidMatchConfiguration() {
        CreateMatchDto dto = new CreateMatchDto();
        dto.setMatchDate(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> matchService.createMatch(TEAM_ID, dto))
                .isInstanceOf(InvalidMatchConfigurationException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void createMatch_teamNotFound_throwsTeamNotFoundException() {
        CreateMatchDto dto = new CreateMatchDto();
        dto.setOpponentName("VC Paris");
        dto.setMatchDate(LocalDateTime.now().plusDays(1));

        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.createMatch(TEAM_ID, dto))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void createMatch_withOpponentName_succeeds() {
        Team team = team();
        CreateMatchDto dto = new CreateMatchDto();
        dto.setOpponentName("VC Paris");
        dto.setMatchDate(LocalDateTime.now().plusDays(1));

        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(matchRepository.save(any())).thenReturn(match(team));

        MatchResponseDto result = matchService.createMatch(TEAM_ID, dto);

        assertThat(result).isNotNull();
        verify(matchRepository).save(any());
    }

    // ── updateMatch ───────────────────────────────────────────────────────────

    @Test
    void updateMatch_notFound_throws() {
        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.empty());

        var dto = new UpdateMatchDto();
        assertThatThrownBy(() -> matchService.updateMatch(MATCH_ID, dto))
                .isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void updateMatch_found_updatesAndReturns() {
        Team team = team();
        Match existing = match(team);
        UpdateMatchDto dto = new UpdateMatchDto();
        dto.setLocation("New Gym");

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(existing));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MatchResponseDto result = matchService.updateMatch(MATCH_ID, dto);

        assertThat(result).isNotNull();
        assertThat(existing.getLocation()).isEqualTo("New Gym");
    }

    @Test
    void updateMatch_forfeit_ourTeamForfeited_createsForfeitSets() {
        Team team = team();
        Match match = match(team); // home = true
        UpdateMatchDto dto = new UpdateMatchDto();
        dto.setStatus(MatchStatus.FORFEIT);
        dto.setForfeitedBy(MatchSide.HOME); // home forfeited = our team

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(matchSetRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        matchService.updateMatch(MATCH_ID, dto);

        verify(matchSetRepository).deleteByMatchId(MATCH_ID);
        verify(matchSetRepository).saveAll(any());
        assertThat(match.getStatus()).isEqualTo(MatchStatus.FORFEIT);
        assertThat(match.getForfeitedBy()).isEqualTo(MatchSide.HOME);
    }

    @Test
    void updateMatch_forfeit_opponentForfeited_createsForfeitSets() {
        Team team = team();
        Match match = match(team); // home = true
        UpdateMatchDto dto = new UpdateMatchDto();
        dto.setStatus(MatchStatus.FORFEIT);
        dto.setForfeitedBy(MatchSide.AWAY); // away forfeited = opponent, we win

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(matchSetRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        matchService.updateMatch(MATCH_ID, dto);

        verify(matchSetRepository).saveAll(any());
    }

    // ── deleteMatch ───────────────────────────────────────────────────────────

    @Test
    void deleteMatch_notFound_throws() {
        when(matchRepository.existsById(MATCH_ID)).thenReturn(false);

        assertThatThrownBy(() -> matchService.deleteMatch(MATCH_ID))
                .isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void deleteMatch_found_deletesById() {
        when(matchRepository.existsById(MATCH_ID)).thenReturn(true);

        matchService.deleteMatch(MATCH_ID);

        verify(matchRepository).deleteById(MATCH_ID);
    }

    // ── addOrUpdateSet ────────────────────────────────────────────────────────

    @Test
    void addOrUpdateSet_newSet_addsToMatch() {
        Team team = team();
        Match match = match(team);
        CreateMatchSetDto dto = setDto(1, 25, 18);

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(matchSetRepository.findByMatchIdAndSetNumber(MATCH_ID, 1)).thenReturn(Optional.empty());
        when(matchSetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MatchResponseDto result = matchService.addOrUpdateSet(MATCH_ID, dto);

        assertThat(result).isNotNull();
        assertThat(match.getSets()).hasSize(1);
    }

    @Test
    void addOrUpdateSet_existingSet_updatesPoints() {
        Team team = team();
        Match match = match(team);
        MatchSet existing = MatchSet.builder().match(match).setNumber(1).teamPoints(20).opponentPoints(25).build();
        match.getSets().add(existing);
        CreateMatchSetDto dto = setDto(1, 25, 20);

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(matchSetRepository.findByMatchIdAndSetNumber(MATCH_ID, 1)).thenReturn(Optional.of(existing));
        when(matchSetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        matchService.addOrUpdateSet(MATCH_ID, dto);

        assertThat(existing.getTeamPoints()).isEqualTo(25);
        assertThat(existing.getOpponentPoints()).isEqualTo(20);
    }

    @Test
    void addOrUpdateSet_threeWins_setsStatusPlayed() {
        Team team = team();
        Match match = match(team);
        match.getSets().add(MatchSet.builder().match(match).setNumber(1).teamPoints(25).opponentPoints(18).build());
        match.getSets().add(MatchSet.builder().match(match).setNumber(2).teamPoints(25).opponentPoints(20).build());
        CreateMatchSetDto dto = setDto(3, 25, 15);

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(matchSetRepository.findByMatchIdAndSetNumber(MATCH_ID, 3)).thenReturn(Optional.empty());
        when(matchSetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        matchService.addOrUpdateSet(MATCH_ID, dto);

        assertThat(match.getStatus()).isEqualTo(MatchStatus.PLAYED);
    }

    // ── deleteSet ─────────────────────────────────────────────────────────────

    @Test
    void deleteSet_removesSetFromMatch() {
        Team team = team();
        Match match = match(team);
        match.getSets().add(MatchSet.builder().match(match).setNumber(1).teamPoints(25).opponentPoints(18).build());

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

        MatchResponseDto result = matchService.deleteSet(MATCH_ID, 1);

        assertThat(result).isNotNull();
        assertThat(match.getSets()).isEmpty();
        verify(matchSetRepository).deleteByMatchIdAndSetNumber(MATCH_ID, 1);
    }

    // ── setAllSets ────────────────────────────────────────────────────────────

    @Test
    void setAllSets_replacesAllAndSetsPlayed() {
        Team team = team();
        Match match = match(team);
        BulkMatchSetsDto dto = new BulkMatchSetsDto();
        dto.setSets(List.of(setDto(1, 25, 18), setDto(2, 25, 20), setDto(3, 25, 15)));

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(matchSetRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MatchResponseDto result = matchService.setAllSets(MATCH_ID, dto);

        assertThat(result).isNotNull();
        assertThat(match.getStatus()).isEqualTo(MatchStatus.PLAYED);
        verify(matchSetRepository).deleteByMatchId(MATCH_ID);
        verify(matchSetRepository).saveAll(any());
    }

    @Test
    void setAllSets_notEnoughWins_doesNotSetPlayed() {
        Team team = team();
        Match match = match(team);
        BulkMatchSetsDto dto = new BulkMatchSetsDto();
        dto.setSets(List.of(setDto(1, 25, 18), setDto(2, 18, 25)));

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(matchSetRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        matchService.setAllSets(MATCH_ID, dto);

        assertThat(match.getStatus()).isEqualTo(MatchStatus.SCHEDULED);
    }

    // ── updateAttendance ──────────────────────────────────────────────────────

    @Test
    void updateAttendance_existingPlayer_updatesStatus() {
        Team team = team();
        Match match = match(team);
        AppUser user = AppUser.builder().id(USER_ID).username("player").role(Role.PLAYER).build();
        MatchPlayerId mpId = new MatchPlayerId(MATCH_ID, USER_ID);
        MatchPlayer matchPlayer = MatchPlayer.builder().id(mpId).match(match).player(user).build();
        match.getPlayers().add(matchPlayer);

        UpdateAttendanceDto dto = new UpdateAttendanceDto();
        dto.setAttendanceStatus(AttendanceStatus.PRESENT);

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(matchPlayerRepository.findById(mpId)).thenReturn(Optional.of(matchPlayer));

        MatchResponseDto result = matchService.updateAttendance(MATCH_ID, USER_ID, dto);

        assertThat(result).isNotNull();
        assertThat(matchPlayer.getAttendanceStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    void updateAttendance_newPlayer_addsToMatch() {
        Team team = team();
        Match match = match(team);
        AppUser user = AppUser.builder().id(USER_ID).username("player").role(Role.PLAYER).build();
        MatchPlayerId mpId = new MatchPlayerId(MATCH_ID, USER_ID);

        UpdateAttendanceDto dto = new UpdateAttendanceDto();
        dto.setAttendanceStatus(AttendanceStatus.ABSENT);

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(matchPlayerRepository.findById(mpId)).thenReturn(Optional.empty());
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        MatchResponseDto result = matchService.updateAttendance(MATCH_ID, USER_ID, dto);

        assertThat(result).isNotNull();
        assertThat(match.getPlayers()).hasSize(1);
    }

    @Test
    void updateAttendance_newPlayerNotFound_throws() {
        Team team = team();
        Match match = match(team);
        MatchPlayerId mpId = new MatchPlayerId(MATCH_ID, USER_ID);

        UpdateAttendanceDto dto = new UpdateAttendanceDto();
        dto.setAttendanceStatus(AttendanceStatus.PRESENT);

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));
        when(matchPlayerRepository.findById(mpId)).thenReturn(Optional.empty());
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.updateAttendance(MATCH_ID, USER_ID, dto))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ── setCaptain ────────────────────────────────────────────────────────────

    @Test
    void setCaptain_matchNotFound_throws() {
        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.setCaptain(MATCH_ID, USER_ID))
                .isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void setCaptain_playerNotInMatch_throws() {
        Team team = team();
        Match match = match(team);

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> matchService.setCaptain(MATCH_ID, USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void setCaptain_success_clearsPreviousCaptainAndSetsNew() {
        Team team = team();
        Match match = match(team);
        AppUser user1 = AppUser.builder().id(USER_ID).username("p1").role(Role.PLAYER).build();
        AppUser user2 = AppUser.builder().id(51L).username("p2").role(Role.PLAYER).build();
        MatchPlayer mp1 = MatchPlayer.builder().id(new MatchPlayerId(MATCH_ID, USER_ID)).match(match).player(user1).captain(true).build();
        MatchPlayer mp2 = MatchPlayer.builder().id(new MatchPlayerId(MATCH_ID, 51L)).match(match).player(user2).captain(false).build();
        match.getPlayers().addAll(List.of(mp1, mp2));

        when(matchRepository.findById(MATCH_ID)).thenReturn(Optional.of(match));

        matchService.setCaptain(MATCH_ID, 51L);

        assertThat(mp1.isCaptain()).isFalse();
        assertThat(mp2.isCaptain()).isTrue();
    }
}
