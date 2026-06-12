package com.skyball.volley.auth.service;

import com.skyball.volley.club.domain.ClubMembershipId;
import com.skyball.volley.club.persistence.ClubMembershipRepository;
import com.skyball.volley.common.MembershipRole;
import com.skyball.volley.match.persistence.MatchRepository;
import com.skyball.volley.team.domain.TeamMembershipId;
import com.skyball.volley.team.persistence.TeamMembershipRepository;
import com.skyball.volley.team.persistence.TeamRepository;
import com.skyball.volley.user.domain.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("sec")
@RequiredArgsConstructor
public class SecurityService {

    private final CurrentUserContext currentUserContext;
    private final ClubMembershipRepository clubMembershipRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    // ── Self ──────────────────────────────────────────────────────────────────

    public boolean isSelf(Long userId) {
        AppUser user = currentUserContext.get();
        return user.isSuperAdmin() || user.getId().equals(userId);
    }

    // ── Club level ────────────────────────────────────────────────────────────

    public boolean isClubAdmin(Long clubId) {
        AppUser user = currentUserContext.get();
        return user.isSuperAdmin() || hasClubRoleAtLeast(user.getId(), clubId, MembershipRole.ADMIN);
    }

    public boolean isClubManager(Long clubId) {
        AppUser user = currentUserContext.get();
        return user.isSuperAdmin() || hasClubRoleAtLeast(user.getId(), clubId, MembershipRole.MANAGER);
    }

    public boolean isClubMember(Long clubId) {
        AppUser user = currentUserContext.get();
        return user.isSuperAdmin() || clubMembershipRepository.existsById(new ClubMembershipId(user.getId(), clubId));
    }

    // ── Team level (team role OR club cascade) ────────────────────────────────

    public boolean isTeamAdmin(Long teamId) {
        AppUser user = currentUserContext.get();
        return user.isSuperAdmin()
                || hasTeamRoleAtLeast(user.getId(), teamId, MembershipRole.ADMIN)
                || isClubAdminOfTeam(user.getId(), teamId);
    }

    public boolean isTeamManager(Long teamId) {
        AppUser user = currentUserContext.get();
        return user.isSuperAdmin()
                || hasTeamRoleAtLeast(user.getId(), teamId, MembershipRole.MANAGER)
                || isClubManagerOfTeam(user.getId(), teamId);
    }

    public boolean isTeamMember(Long teamId) {
        AppUser user = currentUserContext.get();
        return user.isSuperAdmin()
                || teamMembershipRepository.existsById(new TeamMembershipId(user.getId(), teamId))
                || isClubManagerOfTeam(user.getId(), teamId);
    }

    // ── Match level (delegates to team level) ─────────────────────────────────

    public boolean isMatchTeamMember(Long matchId) {
        AppUser user = currentUserContext.get();
        if (user.isSuperAdmin()) return true;
        return matchRepository.findById(matchId)
                .map(m -> isTeamMemberById(user.getId(), m.getTeam().getId()))
                .orElse(false);
    }

    public boolean isTeamManagerOfMatch(Long matchId) {
        AppUser user = currentUserContext.get();
        if (user.isSuperAdmin()) return true;
        return matchRepository.findById(matchId)
                .map(m -> isTeamManagerById(user.getId(), m.getTeam().getId()))
                .orElse(false);
    }

    public boolean isTeamAdminOfMatch(Long matchId) {
        AppUser user = currentUserContext.get();
        if (user.isSuperAdmin()) return true;
        return matchRepository.findById(matchId)
                .map(m -> isTeamAdminById(user.getId(), m.getTeam().getId()))
                .orElse(false);
    }

    // ── Public util ───────────────────────────────────────────────────────────

    public boolean isSuperAdmin() {
        return currentUserContext.get().isSuperAdmin();
    }

    public Long getCurrentUserId() {
        return currentUserContext.get().getId();
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private boolean isClubAdminOfTeam(Long userId, Long teamId) {
        return teamRepository.findById(teamId)
                .filter(t -> t.getClub() != null)
                .map(t -> hasClubRoleAtLeast(userId, t.getClub().getId(), MembershipRole.ADMIN))
                .orElse(false);
    }

    private boolean isClubManagerOfTeam(Long userId, Long teamId) {
        return teamRepository.findById(teamId)
                .filter(t -> t.getClub() != null)
                .map(t -> hasClubRoleAtLeast(userId, t.getClub().getId(), MembershipRole.MANAGER))
                .orElse(false);
    }

    private boolean isTeamAdminById(Long userId, Long teamId) {
        return hasTeamRoleAtLeast(userId, teamId, MembershipRole.ADMIN) || isClubAdminOfTeam(userId, teamId);
    }

    private boolean isTeamManagerById(Long userId, Long teamId) {
        return hasTeamRoleAtLeast(userId, teamId, MembershipRole.MANAGER) || isClubManagerOfTeam(userId, teamId);
    }

    private boolean isTeamMemberById(Long userId, Long teamId) {
        return teamMembershipRepository.existsById(new TeamMembershipId(userId, teamId))
                || isClubManagerOfTeam(userId, teamId);
    }

    private boolean hasClubRoleAtLeast(Long userId, Long clubId, MembershipRole minimum) {
        return clubMembershipRepository
                .findById(new ClubMembershipId(userId, clubId))
                .map(m -> m.getRole().atLeast(minimum))
                .orElse(false);
    }

    private boolean hasTeamRoleAtLeast(Long userId, Long teamId, MembershipRole minimum) {
        return teamMembershipRepository
                .findById(new TeamMembershipId(userId, teamId))
                .map(m -> m.getRole().atLeast(minimum))
                .orElse(false);
    }
}
