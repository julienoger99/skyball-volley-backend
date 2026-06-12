package com.skyball.volley.match.service;

import com.skyball.volley.championship.domain.Championship;
import com.skyball.volley.championship.exception.ChampionshipNotFoundException;
import com.skyball.volley.championship.persistence.ChampionshipRepository;
import com.skyball.volley.match.domain.*;
import com.skyball.volley.match.dto.*;
import com.skyball.volley.match.exception.InvalidMatchConfigurationException;
import com.skyball.volley.match.exception.MatchNotFoundException;
import com.skyball.volley.match.persistence.MatchPlayerRepository;
import com.skyball.volley.match.persistence.MatchRepository;
import com.skyball.volley.match.persistence.MatchSetRepository;
import com.skyball.volley.team.domain.Team;
import com.skyball.volley.team.exception.TeamNotFoundException;
import com.skyball.volley.team.persistence.TeamRepository;
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
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchSetRepository matchSetRepository;
    private final MatchPlayerRepository matchPlayerRepository;
    private final TeamRepository teamRepository;
    private final AppUserRepository appUserRepository;
    private final ChampionshipRepository championshipRepository;

    @PreAuthorize("@sec.isTeamMember(#teamId)")
    @Transactional(readOnly = true)
    public Page<MatchResponseDto> getMatchesByTeam(Long teamId, Pageable pageable) {
        if (!teamRepository.existsById(teamId)) throw new TeamNotFoundException(teamId);
        return matchRepository.findByTeamId(teamId, pageable).map(MatchResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<MatchResponseDto> getMatchesByChampionship(Long championshipId, Pageable pageable) {
        findChampionshipOrThrow(championshipId);
        return matchRepository.findByChampionshipId(championshipId, pageable).map(MatchResponseDto::from);
    }

    @PreAuthorize("@sec.isMatchTeamMember(#id)")
    @Transactional(readOnly = true)
    public MatchResponseDto getMatchById(Long id) {
        return MatchResponseDto.from(findOrThrow(id));
    }

    @PreAuthorize("@sec.isTeamManager(#teamId)")
    @Transactional
    public MatchResponseDto createMatch(Long teamId, CreateMatchDto dto) {
        if (dto.getOpponentTeamId() == null && (dto.getOpponentName() == null || dto.getOpponentName().isBlank())) {
            throw new InvalidMatchConfigurationException("Either opponent team ID or opponent name must be provided");
        }

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));

        Team opponentTeam = null;
        if (dto.getOpponentTeamId() != null) {
            opponentTeam = teamRepository.findById(dto.getOpponentTeamId())
                    .orElseThrow(() -> new TeamNotFoundException(dto.getOpponentTeamId()));
        }

        Championship championship = null;
        if (dto.getChampionshipId() != null) {
            championship = findChampionshipOrThrow(dto.getChampionshipId());
        }

        Match match = Match.builder()
                .team(team)
                .opponentTeam(opponentTeam)
                .opponentName(opponentTeam == null ? dto.getOpponentName() : null)
                .matchDate(dto.getMatchDate())
                .location(dto.getLocation())
                .home(dto.isHome())
                .championship(championship)
                .coachMessage(dto.getCoachMessage())
                .build();

        match = matchRepository.save(match);

        final Match savedMatch = match;
        List<MatchPlayer> players = team.getTeamMemberships().stream()
                .map(tm -> MatchPlayer.builder()
                        .id(new MatchPlayerId(savedMatch.getId(), tm.getUser().getId()))
                        .match(savedMatch)
                        .player(tm.getUser())
                        .build())
                .toList();
        savedMatch.getPlayers().addAll(players); // cascade ALL handles the INSERT at flush time

        return MatchResponseDto.from(savedMatch);
    }

    @PreAuthorize("@sec.isTeamManagerOfMatch(#id)")
    @Transactional
    public MatchResponseDto updateMatch(Long id, UpdateMatchDto dto) {
        Match match = findOrThrow(id);

        if (dto.getMatchDate() != null) match.setMatchDate(dto.getMatchDate());
        if (dto.getLocation() != null) match.setLocation(dto.getLocation());
        if (dto.getHome() != null) match.setHome(dto.getHome());
        if (dto.getCoachMessage() != null) match.setCoachMessage(dto.getCoachMessage());
        if (dto.getStatus() != null) match.setStatus(dto.getStatus());
        if (dto.getForfeitedBy() != null) match.setForfeitedBy(dto.getForfeitedBy());
        if (dto.getChampionshipId() != null) {
            match.setChampionship(findChampionshipOrThrow(dto.getChampionshipId()));
        }

        if (dto.getStatus() == MatchStatus.FORFEIT && dto.getForfeitedBy() != null) {
            match.getSets().clear();
            matchSetRepository.deleteByMatchId(match.getId());
            boolean ourTeamForfeited = match.isHome() == (dto.getForfeitedBy() == MatchSide.HOME);
            List<MatchSet> forfeitSets = java.util.stream.IntStream.rangeClosed(1, 3)
                    .mapToObj(i -> MatchSet.builder()
                            .match(match)
                            .setNumber(i)
                            .teamPoints(ourTeamForfeited ? 0 : 25)
                            .opponentPoints(ourTeamForfeited ? 25 : 0)
                            .build())
                    .toList();
            matchSetRepository.saveAll(forfeitSets);
            match.getSets().addAll(forfeitSets);
        }

        return MatchResponseDto.from(matchRepository.save(match));
    }

    @PreAuthorize("@sec.isTeamAdminOfMatch(#id)")
    @Transactional
    public void deleteMatch(Long id) {
        if (!matchRepository.existsById(id)) throw new MatchNotFoundException(id);
        matchRepository.deleteById(id);
    }

    @PreAuthorize("@sec.isTeamManagerOfMatch(#matchId)")
    @Transactional
    public MatchResponseDto addOrUpdateSet(Long matchId, CreateMatchSetDto dto) {
        Match match = findOrThrow(matchId);

        MatchSet set = matchSetRepository.findByMatchIdAndSetNumber(matchId, dto.getSetNumber())
                .orElse(MatchSet.builder().match(match).setNumber(dto.getSetNumber()).build());

        set.setTeamPoints(dto.getTeamPoints());
        set.setOpponentPoints(dto.getOpponentPoints());
        matchSetRepository.save(set);

        if (!match.getSets().contains(set)) {
            match.getSets().add(set);
        }

        long teamSetsWon = match.getSets().stream().filter(s -> s.getTeamPoints() > s.getOpponentPoints()).count();
        long opponentSetsWon = match.getSets().stream().filter(s -> s.getOpponentPoints() > s.getTeamPoints()).count();
        if (teamSetsWon >= 3 || opponentSetsWon >= 3) {
            match.setStatus(MatchStatus.PLAYED);
            matchRepository.save(match);
        }

        return MatchResponseDto.from(match);
    }

    @PreAuthorize("@sec.isTeamManagerOfMatch(#matchId)")
    @Transactional
    public MatchResponseDto deleteSet(Long matchId, int setNumber) {
        Match match = findOrThrow(matchId);
        matchSetRepository.deleteByMatchIdAndSetNumber(matchId, setNumber);
        match.getSets().removeIf(s -> s.getSetNumber() == setNumber);
        return MatchResponseDto.from(match);
    }

    @PreAuthorize("@sec.isSelf(#userId) or @sec.isTeamManagerOfMatch(#matchId)")
    @Transactional
    public MatchResponseDto updateAttendance(Long matchId, Long userId, UpdateAttendanceDto dto) {
        Match match = findOrThrow(matchId);

        MatchPlayerId matchPlayerId = new MatchPlayerId(matchId, userId);
        MatchPlayer matchPlayer = matchPlayerRepository.findById(matchPlayerId)
                .orElseGet(() -> {
                    var player = appUserRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException(userId));
                    return MatchPlayer.builder()
                            .id(matchPlayerId)
                            .match(match)
                            .player(player)
                            .build();
                });

        matchPlayer.setAttendanceStatus(dto.getAttendanceStatus());

        if (match.getPlayers().stream().noneMatch(p -> p.getId().equals(matchPlayerId))) {
            match.getPlayers().add(matchPlayer);
        }

        return MatchResponseDto.from(match);
    }

    @PreAuthorize("@sec.isTeamManagerOfMatch(#matchId)")
    @Transactional
    public MatchResponseDto setAllSets(Long matchId, BulkMatchSetsDto dto) {
        Match match = findOrThrow(matchId);

        match.getSets().clear();
        matchSetRepository.deleteByMatchId(matchId);

        List<MatchSet> sets = dto.getSets().stream()
                .map(s -> MatchSet.builder()
                        .match(match)
                        .setNumber(s.getSetNumber())
                        .teamPoints(s.getTeamPoints())
                        .opponentPoints(s.getOpponentPoints())
                        .build())
                .toList();
        matchSetRepository.saveAll(sets);
        match.getSets().addAll(sets);

        long teamSetsWon = sets.stream().filter(s -> s.getTeamPoints() > s.getOpponentPoints()).count();
        long opponentSetsWon = sets.stream().filter(s -> s.getOpponentPoints() > s.getTeamPoints()).count();
        if (teamSetsWon >= 3 || opponentSetsWon >= 3) {
            match.setStatus(MatchStatus.PLAYED);
            matchRepository.save(match);
        }

        return MatchResponseDto.from(match);
    }

    @PreAuthorize("@sec.isTeamManagerOfMatch(#matchId)")
    @Transactional
    public MatchResponseDto setCaptain(Long matchId, Long userId) {
        Match match = findOrThrow(matchId);

        match.getPlayers().forEach(p -> p.setCaptain(false));

        MatchPlayer captain = match.getPlayers().stream()
                .filter(p -> p.getId().getPlayerId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(userId));

        captain.setCaptain(true);

        return MatchResponseDto.from(match);
    }

    private Match findOrThrow(Long id) {
        return matchRepository.findById(id).orElseThrow(() -> new MatchNotFoundException(id));
    }

    private Championship findChampionshipOrThrow(Long id) {
        return championshipRepository.findById(id).orElseThrow(() -> new ChampionshipNotFoundException(id));
    }
}
