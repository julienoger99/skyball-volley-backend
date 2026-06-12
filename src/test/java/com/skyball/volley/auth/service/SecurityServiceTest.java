package com.skyball.volley.auth.service;

import com.skyball.volley.club.domain.ClubMembership;
import com.skyball.volley.club.domain.ClubMembershipId;
import com.skyball.volley.club.persistence.ClubMembershipRepository;
import com.skyball.volley.common.MembershipRole;
import com.skyball.volley.match.persistence.MatchRepository;
import com.skyball.volley.team.domain.Team;
import com.skyball.volley.team.domain.TeamMembership;
import com.skyball.volley.team.domain.TeamMembershipId;
import com.skyball.volley.team.persistence.TeamMembershipRepository;
import com.skyball.volley.team.persistence.TeamRepository;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock private CurrentUserContext currentUserContext;
    @Mock private ClubMembershipRepository clubMembershipRepository;
    @Mock private TeamMembershipRepository teamMembershipRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private MatchRepository matchRepository;

    @InjectMocks private SecurityService securityService;

    private static final Long USER_ID = 1L;
    private static final Long CLUB_ID = 10L;
    private static final Long TEAM_ID = 20L;

    private AppUser user(boolean superAdmin) {
        return AppUser.builder().id(USER_ID).username("testuser").role(Role.PLAYER).superAdmin(superAdmin).build();
    }

    private ClubMembership clubMembership(MembershipRole role) {
        return ClubMembership.builder().id(new ClubMembershipId(USER_ID, CLUB_ID)).role(role).build();
    }

    private TeamMembership teamMembership(MembershipRole role) {
        return TeamMembership.builder().id(new TeamMembershipId(USER_ID, TEAM_ID)).role(role).build();
    }

    // ── isSelf ────────────────────────────────────────────────────────────────

    @Test
    void isSelf_superAdmin_alwaysTrue() {
        when(currentUserContext.get()).thenReturn(user(true));
        assertThat(securityService.isSelf(999L)).isTrue();
    }

    @Test
    void isSelf_sameUser_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(false));
        assertThat(securityService.isSelf(USER_ID)).isTrue();
    }

    @Test
    void isSelf_differentUser_returnsFalse() {
        when(currentUserContext.get()).thenReturn(user(false));
        assertThat(securityService.isSelf(999L)).isFalse();
    }

    // ── isClubAdmin ───────────────────────────────────────────────────────────

    @Test
    void isClubAdmin_superAdmin_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(true));
        assertThat(securityService.isClubAdmin(CLUB_ID)).isTrue();
    }

    @Test
    void isClubAdmin_adminRole_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(clubMembershipRepository.findById(new ClubMembershipId(USER_ID, CLUB_ID)))
                .thenReturn(Optional.of(clubMembership(MembershipRole.ADMIN)));
        assertThat(securityService.isClubAdmin(CLUB_ID)).isTrue();
    }

    @Test
    void isClubAdmin_managerRole_returnsFalse() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(clubMembershipRepository.findById(new ClubMembershipId(USER_ID, CLUB_ID)))
                .thenReturn(Optional.of(clubMembership(MembershipRole.MANAGER)));
        assertThat(securityService.isClubAdmin(CLUB_ID)).isFalse();
    }

    @Test
    void isClubAdmin_noMembership_returnsFalse() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(clubMembershipRepository.findById(any())).thenReturn(Optional.empty());
        assertThat(securityService.isClubAdmin(CLUB_ID)).isFalse();
    }

    // ── isClubManager ─────────────────────────────────────────────────────────

    @Test
    void isClubManager_managerRole_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(clubMembershipRepository.findById(new ClubMembershipId(USER_ID, CLUB_ID)))
                .thenReturn(Optional.of(clubMembership(MembershipRole.MANAGER)));
        assertThat(securityService.isClubManager(CLUB_ID)).isTrue();
    }

    @Test
    void isClubManager_adminRole_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(clubMembershipRepository.findById(new ClubMembershipId(USER_ID, CLUB_ID)))
                .thenReturn(Optional.of(clubMembership(MembershipRole.ADMIN)));
        assertThat(securityService.isClubManager(CLUB_ID)).isTrue();
    }

    @Test
    void isClubManager_memberRole_returnsFalse() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(clubMembershipRepository.findById(new ClubMembershipId(USER_ID, CLUB_ID)))
                .thenReturn(Optional.of(clubMembership(MembershipRole.MEMBER)));
        assertThat(securityService.isClubManager(CLUB_ID)).isFalse();
    }

    // ── isClubMember ──────────────────────────────────────────────────────────

    @Test
    void isClubMember_hasMembership_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(clubMembershipRepository.existsById(new ClubMembershipId(USER_ID, CLUB_ID))).thenReturn(true);
        assertThat(securityService.isClubMember(CLUB_ID)).isTrue();
    }

    @Test
    void isClubMember_noMembership_returnsFalse() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(clubMembershipRepository.existsById(new ClubMembershipId(USER_ID, CLUB_ID))).thenReturn(false);
        assertThat(securityService.isClubMember(CLUB_ID)).isFalse();
    }

    // ── isTeamAdmin ───────────────────────────────────────────────────────────

    @Test
    void isTeamAdmin_teamAdminRole_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(teamMembershipRepository.findById(new TeamMembershipId(USER_ID, TEAM_ID)))
                .thenReturn(Optional.of(teamMembership(MembershipRole.ADMIN)));
        assertThat(securityService.isTeamAdmin(TEAM_ID)).isTrue();
    }

    @Test
    void isTeamAdmin_clubAdminCascade_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(teamMembershipRepository.findById(new TeamMembershipId(USER_ID, TEAM_ID))).thenReturn(Optional.empty());
        Team team = Team.builder().id(TEAM_ID).name("T").build();
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        assertThat(securityService.isTeamAdmin(TEAM_ID)).isFalse();
    }

    @Test
    void isTeamAdmin_clubAdminCascadeWithClub_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(teamMembershipRepository.findById(new TeamMembershipId(USER_ID, TEAM_ID))).thenReturn(Optional.empty());
        var club = com.skyball.volley.club.domain.Club.builder().id(CLUB_ID).name("C").build();
        Team team = Team.builder().id(TEAM_ID).name("T").club(club).build();
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(clubMembershipRepository.findById(new ClubMembershipId(USER_ID, CLUB_ID)))
                .thenReturn(Optional.of(clubMembership(MembershipRole.ADMIN)));
        assertThat(securityService.isTeamAdmin(TEAM_ID)).isTrue();
    }

    @Test
    void isTeamAdmin_managerRole_returnsFalse() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(teamMembershipRepository.findById(new TeamMembershipId(USER_ID, TEAM_ID)))
                .thenReturn(Optional.of(teamMembership(MembershipRole.MANAGER)));
        assertThat(securityService.isTeamAdmin(TEAM_ID)).isFalse();
    }

    // ── isTeamMember ──────────────────────────────────────────────────────────

    @Test
    void isTeamMember_hasTeamMembership_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(teamMembershipRepository.existsById(new TeamMembershipId(USER_ID, TEAM_ID))).thenReturn(true);
        assertThat(securityService.isTeamMember(TEAM_ID)).isTrue();
    }

    @Test
    void isTeamMember_clubManagerCascade_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(teamMembershipRepository.existsById(new TeamMembershipId(USER_ID, TEAM_ID))).thenReturn(false);
        var club = com.skyball.volley.club.domain.Club.builder().id(CLUB_ID).name("C").build();
        Team team = Team.builder().id(TEAM_ID).name("T").club(club).build();
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(clubMembershipRepository.findById(new ClubMembershipId(USER_ID, CLUB_ID)))
                .thenReturn(Optional.of(clubMembership(MembershipRole.MANAGER)));
        assertThat(securityService.isTeamMember(TEAM_ID)).isTrue();
    }

    @Test
    void isTeamMember_noMembership_returnsFalse() {
        when(currentUserContext.get()).thenReturn(user(false));
        when(teamMembershipRepository.existsById(new TeamMembershipId(USER_ID, TEAM_ID))).thenReturn(false);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());
        assertThat(securityService.isTeamMember(TEAM_ID)).isFalse();
    }

    // ── isSuperAdmin ──────────────────────────────────────────────────────────

    @Test
    void isSuperAdmin_whenFlagTrue_returnsTrue() {
        when(currentUserContext.get()).thenReturn(user(true));
        assertThat(securityService.isSuperAdmin()).isTrue();
    }

    @Test
    void isSuperAdmin_whenFlagFalse_returnsFalse() {
        when(currentUserContext.get()).thenReturn(user(false));
        assertThat(securityService.isSuperAdmin()).isFalse();
    }
}
