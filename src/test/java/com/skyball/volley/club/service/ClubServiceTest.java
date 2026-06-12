package com.skyball.volley.club.service;

import com.skyball.volley.auth.service.SecurityService;
import com.skyball.volley.club.domain.Club;
import com.skyball.volley.club.domain.ClubMembership;
import com.skyball.volley.club.domain.ClubMembershipId;
import com.skyball.volley.club.dto.CreateClubDto;
import com.skyball.volley.club.dto.UpdateClubDto;
import com.skyball.volley.club.exception.ClubAlreadyExistsException;
import com.skyball.volley.club.exception.ClubNotFoundException;
import com.skyball.volley.club.exception.InvalidClubMembershipException;
import com.skyball.volley.club.exception.UserAlreadyInClubException;
import com.skyball.volley.club.persistence.ClubMembershipRepository;
import com.skyball.volley.club.persistence.ClubRepository;
import com.skyball.volley.common.MembershipRole;
import com.skyball.volley.common.UpdateMemberRoleDto;
import com.skyball.volley.team.persistence.TeamMembershipRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock private ClubRepository clubRepository;
    @Mock private AppUserRepository appUserRepository;
    @Mock private ClubMembershipRepository clubMembershipRepository;
    @Mock private TeamMembershipRepository teamMembershipRepository;
    @Mock private SecurityService securityService;

    @InjectMocks private ClubService clubService;

    private static final Long USER_ID = 1L;
    private static final Long CLUB_ID = 10L;

    private AppUser user() {
        return AppUser.builder().id(USER_ID).username("user").role(Role.PLAYER).build();
    }

    private Club club() {
        return Club.builder().id(CLUB_ID).name("Club Volley").city("Paris").build();
    }

    // ── createClub ────────────────────────────────────────────────────────────

    @Test
    void createClub_userAlreadyInClub_throws() {
        when(securityService.getCurrentUserId()).thenReturn(USER_ID);
        when(clubMembershipRepository.existsByIdUserId(USER_ID)).thenReturn(true);

        var dto = new CreateClubDto();
        dto.setName("New Club");
        dto.setCity("Lyon");
        assertThatThrownBy(() -> clubService.createClub(dto))
                .isInstanceOf(UserAlreadyInClubException.class);

        verify(clubRepository, never()).save(any());
    }

    @Test
    void createClub_nameAlreadyExists_throws() {
        when(securityService.getCurrentUserId()).thenReturn(USER_ID);
        when(clubMembershipRepository.existsByIdUserId(USER_ID)).thenReturn(false);
        when(clubRepository.existsByName("Existing Club")).thenReturn(true);

        var dto = new CreateClubDto();
        dto.setName("Existing Club");
        dto.setCity("Lyon");
        assertThatThrownBy(() -> clubService.createClub(dto))
                .isInstanceOf(ClubAlreadyExistsException.class);

        verify(clubRepository, never()).save(any());
    }

    @Test
    void createClub_success_savesClubAndAdminMembership() {
        when(securityService.getCurrentUserId()).thenReturn(USER_ID);
        when(clubMembershipRepository.existsByIdUserId(USER_ID)).thenReturn(false);
        when(clubRepository.existsByName("New Club")).thenReturn(false);
        Club saved = Club.builder().id(CLUB_ID).name("New Club").city("Lyon").build();
        when(clubRepository.save(any())).thenReturn(saved);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));

        var dto = new CreateClubDto();
        dto.setName("New Club");
        dto.setCity("Lyon");
        var result = clubService.createClub(dto);

        assertThat(result.getId()).isEqualTo(CLUB_ID);
        assertThat(result.getName()).isEqualTo("New Club");

        var membershipCaptor = ArgumentCaptor.forClass(ClubMembership.class);
        verify(clubMembershipRepository).save(membershipCaptor.capture());
        assertThat(membershipCaptor.getValue().getRole()).isEqualTo(MembershipRole.ADMIN);
        assertThat(membershipCaptor.getValue().getId().getUserId()).isEqualTo(USER_ID);
    }

    // ── joinClub ──────────────────────────────────────────────────────────────

    @Test
    void joinClub_userAlreadyInClub_throws() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(club()));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(clubMembershipRepository.existsByIdUserId(USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> clubService.joinClub(CLUB_ID, USER_ID))
                .isInstanceOf(UserAlreadyInClubException.class);

        verify(clubMembershipRepository, never()).save(any());
    }

    @Test
    void joinClub_clubNotFound_throws() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubService.joinClub(CLUB_ID, USER_ID))
                .isInstanceOf(ClubNotFoundException.class);
    }

    @Test
    void joinClub_userNotFound_throws() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(club()));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubService.joinClub(CLUB_ID, USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void joinClub_success_savesMemberMembership() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(club()));
        AppUser u = user();
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(clubMembershipRepository.existsByIdUserId(USER_ID)).thenReturn(false);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(u));

        clubService.joinClub(CLUB_ID, USER_ID);

        var captor = ArgumentCaptor.forClass(ClubMembership.class);
        verify(clubMembershipRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(MembershipRole.MEMBER);
    }

    // ── leaveClub ─────────────────────────────────────────────────────────────

    @Test
    void leaveClub_clubNotFound_throws() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubService.leaveClub(CLUB_ID, USER_ID))
                .isInstanceOf(ClubNotFoundException.class);
    }

    @Test
    void leaveClub_userNotFound_throws() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(club()));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clubService.leaveClub(CLUB_ID, USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void leaveClub_notAMember_throws() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(club()));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(clubMembershipRepository.existsById(new ClubMembershipId(USER_ID, CLUB_ID))).thenReturn(false);

        assertThatThrownBy(() -> clubService.leaveClub(CLUB_ID, USER_ID))
                .isInstanceOf(InvalidClubMembershipException.class);
    }

    @Test
    void leaveClub_success_deletesTeamAndClubMemberships() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(club()));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));
        when(clubMembershipRepository.existsById(new ClubMembershipId(USER_ID, CLUB_ID))).thenReturn(true);

        clubService.leaveClub(CLUB_ID, USER_ID);

        verify(teamMembershipRepository).deleteByUserIdAndTeamClubId(USER_ID, CLUB_ID);
        verify(clubMembershipRepository).deleteById(new ClubMembershipId(USER_ID, CLUB_ID));
    }

    // ── updateMemberRole ──────────────────────────────────────────────────────

    @Test
    void updateMemberRole_membershipNotFound_throws() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(club()));
        when(clubMembershipRepository.findById(new ClubMembershipId(USER_ID, CLUB_ID))).thenReturn(Optional.empty());

        var dto = new UpdateMemberRoleDto();
        dto.setRole(MembershipRole.MANAGER);
        assertThatThrownBy(() -> clubService.updateMemberRole(CLUB_ID, USER_ID, dto))
                .isInstanceOf(InvalidClubMembershipException.class);
    }

    @Test
    void updateMemberRole_success_updatesRole() {
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(club()));
        ClubMembership membership = ClubMembership.builder()
                .id(new ClubMembershipId(USER_ID, CLUB_ID))
                .user(user())
                .club(club())
                .role(MembershipRole.MEMBER)
                .build();
        when(clubMembershipRepository.findById(new ClubMembershipId(USER_ID, CLUB_ID)))
                .thenReturn(Optional.of(membership));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user()));

        var dto = new UpdateMemberRoleDto();
        dto.setRole(MembershipRole.MANAGER);
        clubService.updateMemberRole(CLUB_ID, USER_ID, dto);

        assertThat(membership.getRole()).isEqualTo(MembershipRole.MANAGER);
        verify(clubMembershipRepository).save(membership);
    }

    // ── deleteClub ────────────────────────────────────────────────────────────

    @Test
    void deleteClub_notFound_throws() {
        when(clubRepository.existsById(CLUB_ID)).thenReturn(false);

        assertThatThrownBy(() -> clubService.deleteClub(CLUB_ID))
                .isInstanceOf(ClubNotFoundException.class);
    }

    @Test
    void deleteClub_success_deletesById() {
        when(clubRepository.existsById(CLUB_ID)).thenReturn(true);

        clubService.deleteClub(CLUB_ID);

        verify(clubRepository).deleteById(CLUB_ID);
    }

    // ── updateClub ────────────────────────────────────────────────────────────

    @Test
    void updateClub_nameConflict_throws() {
        Club existing = Club.builder().id(CLUB_ID).name("Old Name").city("Paris").build();
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(existing));
        when(clubRepository.existsByName("Taken Name")).thenReturn(true);

        var dto = new UpdateClubDto();
        dto.setName("Taken Name");
        assertThatThrownBy(() -> clubService.updateClub(CLUB_ID, dto))
                .isInstanceOf(ClubAlreadyExistsException.class);
    }

    @Test
    void updateClub_sameNameNoConflictCheck() {
        Club existing = Club.builder().id(CLUB_ID).name("Same Name").city("Paris").build();
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(existing));
        when(clubRepository.save(any())).thenReturn(existing);

        var dto = new UpdateClubDto();
        dto.setName("Same Name");
        dto.setCity("Lyon");
        clubService.updateClub(CLUB_ID, dto);

        verify(clubRepository, never()).existsByName(any());
        verify(clubRepository).save(existing);
    }
}
