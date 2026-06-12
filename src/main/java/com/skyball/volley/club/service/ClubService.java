package com.skyball.volley.club.service;

import com.skyball.volley.club.domain.Club;
import com.skyball.volley.club.domain.ClubMembership;
import com.skyball.volley.club.domain.ClubMembershipId;
import com.skyball.volley.club.dto.ClubMemberDto;
import com.skyball.volley.club.dto.ClubResponseDto;
import com.skyball.volley.club.dto.CreateClubDto;
import com.skyball.volley.club.dto.UpdateClubDto;
import com.skyball.volley.club.exception.ClubAlreadyExistsException;
import com.skyball.volley.club.exception.ClubNotFoundException;
import com.skyball.volley.club.exception.InvalidClubMembershipException;
import com.skyball.volley.club.exception.UserAlreadyInClubException;
import com.skyball.volley.auth.service.SecurityService;
import com.skyball.volley.club.persistence.ClubMembershipRepository;
import com.skyball.volley.club.persistence.ClubRepository;
import com.skyball.volley.common.MembershipRole;
import com.skyball.volley.common.UpdateMemberRoleDto;
import com.skyball.volley.team.persistence.TeamMembershipRepository;
import com.skyball.volley.user.domain.AppUser;
import com.skyball.volley.user.dto.UserResponseDto;
import com.skyball.volley.user.exception.UserNotFoundException;
import com.skyball.volley.user.persistence.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final AppUserRepository appUserRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final SecurityService securityService;

    @Transactional(readOnly = true)
    public Page<ClubResponseDto> getAllClubs(Pageable pageable) {
        return clubRepository.findAll(pageable).map(ClubResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ClubResponseDto getClubById(Long id) {
        return ClubResponseDto.from(findOrThrow(id));
    }

    @PreAuthorize("@sec.isClubMember(#clubId)")
    @Transactional(readOnly = true)
    public List<ClubMemberDto> getClubMembers(Long clubId) {
        findOrThrow(clubId);
        return clubMembershipRepository.findByClubId(clubId).stream()
                .map(ClubMemberDto::from)
                .toList();
    }

    @Transactional
    public ClubResponseDto createClub(CreateClubDto dto) {
        Long currentUserId = securityService.getCurrentUserId();

        if (clubMembershipRepository.existsByIdUserId(currentUserId)) {
            throw new UserAlreadyInClubException(currentUserId);
        }

        if (clubRepository.existsByName(dto.getName())) {
            throw new ClubAlreadyExistsException(dto.getName());
        }

        Club club = Club.builder()
                .name(dto.getName())
                .city(dto.getCity())
                .logoUrl(dto.getLogoUrl())
                .description(dto.getDescription())
                .websiteUrl(dto.getWebsiteUrl())
                .createdAt(LocalDate.now())
                .build();

        club = clubRepository.save(club);

        AppUser creator = appUserRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
        ClubMembership adminMembership = ClubMembership.builder()
                .id(new ClubMembershipId(currentUserId, club.getId()))
                .user(creator)
                .club(club)
                .role(MembershipRole.ADMIN)
                .build();
        clubMembershipRepository.save(adminMembership);

        return ClubResponseDto.from(club);
    }

    @PreAuthorize("@sec.isClubAdmin(#id)")
    @Transactional
    public ClubResponseDto updateClub(Long id, UpdateClubDto dto) {
        Club club = findOrThrow(id);

        if (dto.getName() != null && !dto.getName().equals(club.getName())) {
            if (clubRepository.existsByName(dto.getName())) {
                throw new ClubAlreadyExistsException(dto.getName());
            }
            club.setName(dto.getName());
        }
        if (dto.getCity() != null) club.setCity(dto.getCity());
        if (dto.getLogoUrl() != null) club.setLogoUrl(dto.getLogoUrl());
        if (dto.getDescription() != null) club.setDescription(dto.getDescription());
        if (dto.getWebsiteUrl() != null) club.setWebsiteUrl(dto.getWebsiteUrl());

        return ClubResponseDto.from(clubRepository.save(club));
    }

    @PreAuthorize("@sec.isClubAdmin(#id)")
    @Transactional
    public void deleteClub(Long id) {
        if (!clubRepository.existsById(id)) {
            throw new ClubNotFoundException(id);
        }
        clubRepository.deleteById(id);
    }

    @PreAuthorize("@sec.isSelf(#userId)")
    @Transactional
    public UserResponseDto joinClub(Long clubId, Long userId) {
        Club club = findOrThrow(clubId);
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (clubMembershipRepository.existsByIdUserId(userId)) {
            throw new UserAlreadyInClubException(userId);
        }

        ClubMembershipId membershipId = new ClubMembershipId(userId, clubId);

        ClubMembership membership = ClubMembership.builder()
                .id(membershipId)
                .user(user)
                .club(club)
                .role(MembershipRole.MEMBER)
                .build();
        clubMembershipRepository.save(membership);

        return UserResponseDto.from(user);
    }

    @PreAuthorize("@sec.isClubAdmin(#clubId)")
    @Transactional
    public UserResponseDto updateMemberRole(Long clubId, Long userId, UpdateMemberRoleDto dto) {
        findOrThrow(clubId);
        ClubMembershipId membershipId = new ClubMembershipId(userId, clubId);
        ClubMembership membership = clubMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new InvalidClubMembershipException("User " + userId + " is not a member of club " + clubId));
        membership.setRole(dto.getRole());
        clubMembershipRepository.save(membership);
        return UserResponseDto.from(appUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId)));
    }

    @PreAuthorize("@sec.isSelf(#userId) or @sec.isClubAdmin(#clubId)")
    @Transactional
    public void leaveClub(Long clubId, Long userId) {
        findOrThrow(clubId);
        appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        ClubMembershipId membershipId = new ClubMembershipId(userId, clubId);
        if (!clubMembershipRepository.existsById(membershipId)) {
            throw new InvalidClubMembershipException("User " + userId + " does not belong to club " + clubId);
        }

        teamMembershipRepository.deleteByUserIdAndTeamClubId(userId, clubId);
        clubMembershipRepository.deleteById(membershipId);
    }

    private Club findOrThrow(Long id) {
        return clubRepository.findById(id)
                .orElseThrow(() -> new ClubNotFoundException(id));
    }
}
