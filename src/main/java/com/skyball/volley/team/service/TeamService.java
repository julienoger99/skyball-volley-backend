package com.skyball.volley.team.service;

import com.skyball.volley.auth.service.SecurityService;
import com.skyball.volley.club.domain.Club;
import com.skyball.volley.club.domain.ClubMembership;
import com.skyball.volley.club.domain.ClubMembershipId;
import com.skyball.volley.club.exception.ClubNotFoundException;
import com.skyball.volley.club.persistence.ClubMembershipRepository;
import com.skyball.volley.club.persistence.ClubRepository;
import com.skyball.volley.common.MembershipRole;
import com.skyball.volley.common.UpdateMemberRoleDto;
import com.skyball.volley.team.domain.Team;
import com.skyball.volley.team.domain.TeamCategory;
import com.skyball.volley.team.domain.TeamGender;
import com.skyball.volley.team.domain.TeamMembership;
import com.skyball.volley.team.domain.TeamMembershipId;
import com.skyball.volley.team.dto.CreateTeamDto;
import com.skyball.volley.team.dto.TeamResponseDto;
import com.skyball.volley.team.dto.UpdateTeamDto;
import com.skyball.volley.team.exception.InvalidTeamConfigurationException;
import com.skyball.volley.team.exception.TeamNotFoundException;
import com.skyball.volley.team.persistence.TeamMembershipRepository;
import com.skyball.volley.team.persistence.TeamRepository;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.exception.UserNotFoundException;
import com.skyball.volley.user.persistence.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final ClubRepository clubRepository;
    private final AppUserRepository appUserRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final SecurityService securityService;

    @Transactional(readOnly = true)
    public Page<TeamResponseDto> getAllTeams(Pageable pageable) {
        return teamRepository.findAll(pageable).map(TeamResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<TeamResponseDto> getTeamsByClub(Long clubId, Pageable pageable) {
        if (!clubRepository.existsById(clubId)) {
            throw new ClubNotFoundException(clubId);
        }
        return teamRepository.findByClubId(clubId, pageable).map(TeamResponseDto::from);
    }

    @Transactional(readOnly = true)
    public TeamResponseDto getTeamById(Long id) {
        return TeamResponseDto.from(findOrThrow(id));
    }

    @PreAuthorize("#dto.clubId == null or @sec.isClubAdmin(#dto.clubId)")
    @Transactional
    public TeamResponseDto createTeam(CreateTeamDto dto) {
        validateCategoryGender(dto.getCategory(), dto.getGender());

        Long currentUserId = securityService.getCurrentUserId();
        Club club = null;
        if (dto.getClubId() != null) {
            club = clubRepository.findById(dto.getClubId())
                    .orElseThrow(() -> new ClubNotFoundException(dto.getClubId()));
        } else {
            if (teamMembershipRepository.existsByIdUserIdAndTeamClubIsNullAndRole(currentUserId, MembershipRole.ADMIN)) {
                throw new InvalidTeamConfigurationException("User already owns an independent team");
            }
        }

        Team team = Team.builder()
                .name(dto.getName())
                .category(dto.getCategory())
                .gender(dto.getGender())
                .logoUrl(dto.getLogoUrl())
                .club(club)
                .build();

        team = teamRepository.save(team);

        if (dto.getClubId() == null) {
            AppUser creator = appUserRepository.findById(currentUserId)
                    .orElseThrow(() -> new UserNotFoundException(currentUserId));
            TeamMembership adminMembership = TeamMembership.builder()
                    .id(new TeamMembershipId(currentUserId, team.getId()))
                    .user(creator)
                    .team(team)
                    .role(MembershipRole.ADMIN)
                    .build();
            teamMembershipRepository.save(adminMembership);
        }

        return TeamResponseDto.from(teamRepository.findById(team.getId()).orElseThrow());
    }

    @PreAuthorize("@sec.isTeamManager(#id)")
    @Transactional
    public TeamResponseDto updateTeam(Long id, UpdateTeamDto dto) {
        Team team = findOrThrow(id);

        TeamCategory newCategory = dto.getCategory() != null ? dto.getCategory() : team.getCategory();
        TeamGender newGender = dto.getGender() != null ? dto.getGender() : team.getGender();
        validateCategoryGender(newCategory, newGender);

        if (dto.getName() != null) team.setName(dto.getName());
        if (dto.getLogoUrl() != null) team.setLogoUrl(dto.getLogoUrl());
        team.setCategory(newCategory);
        team.setGender(newGender);

        return TeamResponseDto.from(teamRepository.save(team));
    }

    @PreAuthorize("@sec.isTeamAdmin(#id)")
    @Transactional
    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new TeamNotFoundException(id);
        }
        teamRepository.deleteById(id);
    }

    @PreAuthorize("@sec.isSelf(#userId) or @sec.isTeamManager(#teamId)")
    @Transactional
    public TeamResponseDto addMember(Long teamId, Long userId) {
        Team team = findOrThrow(teamId);
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (team.getClub() != null) {
            boolean inThisClub = clubMembershipRepository.existsById(new ClubMembershipId(userId, team.getClub().getId()));
            if (!inThisClub) {
                if (clubMembershipRepository.existsByIdUserId(userId)) {
                    throw new InvalidTeamConfigurationException("User belongs to a different club");
                }
                // Joueur sans club : auto-rejoindre le club de l'équipe
                clubMembershipRepository.save(ClubMembership.builder()
                        .id(new ClubMembershipId(userId, team.getClub().getId()))
                        .user(user)
                        .club(team.getClub())
                        .role(MembershipRole.MEMBER)
                        .build());
            }
        }

        TeamMembershipId membershipId = new TeamMembershipId(userId, teamId);
        if (!teamMembershipRepository.existsById(membershipId)) {
            teamMembershipRepository.save(TeamMembership.builder()
                    .id(membershipId)
                    .user(user)
                    .team(team)
                    .role(MembershipRole.MEMBER)
                    .build());
        }

        return TeamResponseDto.fromWithoutMembers(team);
    }

    @PreAuthorize("@sec.isTeamAdmin(#teamId)")
    @Transactional
    public TeamResponseDto updateMemberRole(Long teamId, Long userId, UpdateMemberRoleDto dto) {
        Team team = findOrThrow(teamId);
        TeamMembershipId membershipId = new TeamMembershipId(userId, teamId);
        TeamMembership membership = teamMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new InvalidTeamConfigurationException("User " + userId + " is not a member of team " + teamId));
        membership.setRole(dto.getRole());
        teamMembershipRepository.save(membership);
        return TeamResponseDto.fromWithoutMembers(team);
    }

    @PreAuthorize("@sec.isSelf(#userId) or @sec.isTeamManager(#teamId)")
    @Transactional
    public TeamResponseDto removeMember(Long teamId, Long userId) {
        Team team = findOrThrow(teamId);
        appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        teamMembershipRepository.deleteById(new TeamMembershipId(userId, teamId));
        return TeamResponseDto.fromWithoutMembers(team);
    }

    private void validateCategoryGender(TeamCategory category, TeamGender gender) {
        boolean isLoisir = category == TeamCategory.LOISIR;
        boolean isMixed = gender == TeamGender.MIXED;
        if (isLoisir != isMixed) {
            throw new InvalidTeamConfigurationException(
                    "LOISIR category requires MIXED gender and MIXED gender requires LOISIR category"
            );
        }
    }

    private Team findOrThrow(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new TeamNotFoundException(id));
    }
}
