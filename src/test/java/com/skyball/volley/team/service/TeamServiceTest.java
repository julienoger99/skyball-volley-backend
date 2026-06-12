package com.skyball.volley.team.service;

import com.skyball.volley.auth.service.SecurityService;
import com.skyball.volley.club.domain.Club;
import com.skyball.volley.club.domain.ClubMembership;
import com.skyball.volley.club.domain.ClubMembershipId;
import com.skyball.volley.club.exception.ClubNotFoundException;
import com.skyball.volley.club.persistence.ClubMembershipRepository;
import com.skyball.volley.club.persistence.ClubRepository;
import com.skyball.volley.common.MembershipRole;
import com.skyball.volley.team.domain.*;
import com.skyball.volley.team.dto.CreateTeamDto;
import com.skyball.volley.team.exception.InvalidTeamConfigurationException;
import com.skyball.volley.team.exception.TeamNotFoundException;
import com.skyball.volley.team.persistence.TeamMembershipRepository;
import com.skyball.volley.team.persistence.TeamRepository;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.domain.Role;
import com.skyball.volley.user.exception.UserNotFoundException;
import com.skyball.volley.user.persistence.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long CLUB_ID = 10L;
    private static final Long TEAM_ID = 20L;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private ClubMembershipRepository clubMembershipRepository;
    @Mock
    private TeamMembershipRepository teamMembershipRepository;
    @Mock
    private SecurityService securityService;
    @InjectMocks
    private TeamService teamService;

    private AppUser user() {
        return AppUser.builder().id(USER_ID).username("user").role(Role.PLAYER).build();
    }

    private Club club() {
        return Club.builder().id(CLUB_ID).name("Club A").city("Paris").build();
    }

    private Team team(Club club) {
        return Team.builder().id(TEAM_ID).name("Équipe A")
                .category(TeamCategory.SENIOR).gender(TeamGender.MALE)
                .club(club).teamMemberships(new ArrayList<>()).build();
    }

    private CreateTeamDto dto(Long clubId, TeamCategory cat, TeamGender gen) {
        var d = new CreateTeamDto();
        d.setName("Équipe A");
        d.setCategory(cat);
        d.setGender(gen);
        d.setClubId(clubId);
        return d;
    }

    // ── createTeam — validation catégorie/genre ───────────────────────────────

    @Test
    void createTeam_loisirWithMale_throws() {
        var d = dto(CLUB_ID, TeamCategory.LOISIR, TeamGender.MALE);
        assertThatThrownBy(() -> teamService.createTeam(d))
                .isInstanceOf(InvalidTeamConfigurationException.class);
    }

    @Test
    void createTeam_seniorWithMixed_throws() {
        var d = dto(CLUB_ID, TeamCategory.SENIOR, TeamGender.MIXED);
        assertThatThrownBy(() -> teamService.createTeam(d))
                .isInstanceOf(InvalidTeamConfigurationException.class);
    }

    // ── createTeam — team de club ─────────────────────────────────────────────

    @Test
    void createTeam_clubNotFound_throws() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.empty());

        var d = dto(CLUB_ID, TeamCategory.SENIOR, TeamGender.MALE);
        assertThatThrownBy(() -> teamService.createTeam(d))
                .isInstanceOf(ClubNotFoundException.class);
    }

    @Test
    void createTeam_withClub_success_noAdminMembership() {
        Club club = club();
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(club));
        Team saved = team(club);
        when(teamRepository.save(any())).thenReturn(saved);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(saved));

        teamService.createTeam(dto(CLUB_ID, TeamCategory.SENIOR, TeamGender.MALE));

        verify(teamMembershipRepository, never()).save(any());
    }

    // ── createTeam — team indépendante ────────────────────────────────────────

    @Test
    void createTeam_indie_userAlreadyOwnsIndieTeam_throws() {
        when(securityService.getCurrentUserId()).thenReturn(USER_ID);
        when(teamMembershipRepository.existsByIdUserIdAndTeamClubIsNullAndRole(USER_ID, MembershipRole.ADMIN))
                .thenReturn(true);

        var d = dto(null, TeamCategory.SENIOR, TeamGender.MALE);
        assertThatThrownBy(() -> teamService.createTeam(d))
                .isInstanceOf(InvalidTeamConfigurationException.class);

        verify(teamRepository, never()).save(any());
    }

    @Test
    void createTeam_indie_success_savesAdminMembership() {
        when(securityService.getCurrentUserId()).thenReturn(USER_ID);
        when(teamMembershipRepository.existsByIdUserIdAndTeamClubIsNullAndRole(USER_ID, MembershipRole.ADMIN))
                .thenReturn(false);
        Team saved = team(null);
        when(teamRepository.save(any())).thenReturn(saved);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(saved));

        teamService.createTeam(dto(null, TeamCategory.SENIOR, TeamGender.MALE));

        var captor = ArgumentCaptor.forClass(TeamMembership.class);
        verify(teamMembershipRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(MembershipRole.ADMIN);
        assertThat(captor.getValue().getId().getUserId()).isEqualTo(USER_ID);
    }

    // ── addMember ─────────────────────────────────────────────────────────────

    @Test
    void addMember_teamNotFound_throws() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.addMember(TEAM_ID, USER_ID))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void addMember_userNotFound_throws() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(club())));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.addMember(TEAM_ID, USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addMember_userInDifferentClub_throws() {
        Club club = club();
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(club)));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(clubMembershipRepository.existsById(new ClubMembershipId(USER_ID, CLUB_ID))).thenReturn(false);
        when(clubMembershipRepository.existsByIdUserId(USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> teamService.addMember(TEAM_ID, USER_ID))
                .isInstanceOf(InvalidTeamConfigurationException.class);
    }

    @Test
    void addMember_userHasNoClub_autoJoinsClub() {
        Club club = club();
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(club)));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(clubMembershipRepository.existsById(new ClubMembershipId(USER_ID, CLUB_ID))).thenReturn(false);
        when(clubMembershipRepository.existsByIdUserId(USER_ID)).thenReturn(false);
        when(teamMembershipRepository.existsById(new TeamMembershipId(USER_ID, TEAM_ID))).thenReturn(false);

        teamService.addMember(TEAM_ID, USER_ID);

        var clubCaptor = ArgumentCaptor.forClass(ClubMembership.class);
        verify(clubMembershipRepository).save(clubCaptor.capture());
        assertThat(clubCaptor.getValue().getId().getClubId()).isEqualTo(CLUB_ID);
        assertThat(clubCaptor.getValue().getRole()).isEqualTo(MembershipRole.MEMBER);
        verify(teamMembershipRepository).save(any());
    }

    @Test
    void addMember_indieTeam_skipClubCheck() {
        Team indieTeam = team(null);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(indieTeam));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(teamMembershipRepository.existsById(new TeamMembershipId(USER_ID, TEAM_ID))).thenReturn(false);

        teamService.addMember(TEAM_ID, USER_ID);

        verify(clubMembershipRepository, never()).existsById(any());
        verify(teamMembershipRepository).save(any());
    }

    @Test
    void addMember_alreadyMember_noDoubleSave() {
        Club club = club();
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team(club)));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(clubMembershipRepository.existsById(new ClubMembershipId(USER_ID, CLUB_ID))).thenReturn(true);
        when(teamMembershipRepository.existsById(new TeamMembershipId(USER_ID, TEAM_ID))).thenReturn(true);

        teamService.addMember(TEAM_ID, USER_ID);

        verify(teamMembershipRepository, never()).save(any());
    }

    // ── deleteTeam ────────────────────────────────────────────────────────────

    @Test
    void deleteTeam_notFound_throws() {
        when(teamRepository.existsById(TEAM_ID)).thenReturn(false);

        assertThatThrownBy(() -> teamService.deleteTeam(TEAM_ID))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void deleteTeam_success() {
        when(teamRepository.existsById(TEAM_ID)).thenReturn(true);

        teamService.deleteTeam(TEAM_ID);

        verify(teamRepository).deleteById(TEAM_ID);
    }
}
