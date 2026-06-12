package com.skyball.volley.match.dto;

import com.skyball.volley.championship.dto.ChampionshipResponseDto;
import com.skyball.volley.match.domain.Match;
import com.skyball.volley.match.domain.MatchSide;
import com.skyball.volley.team.dto.TeamSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "Match information")
public class MatchResponseDto {

    @Schema(description = "Match ID", example = "1")
    private Long id;

    @Schema(description = "Team this match belongs to")
    private TeamSummaryDto team;

    @Schema(description = "Opponent team ID if registered in the system", example = "2")
    private Long opponentTeamId;

    @Schema(description = "Home team name", example = "Skyball Volley")
    private String homeTeamName;

    @Schema(description = "Away team name", example = "VC Marseille")
    private String awayTeamName;

    @Schema(description = "Match date and time")
    private LocalDateTime matchDate;

    @Schema(description = "Venue", example = "Gymnase Marcel Cerdan")
    private String location;

    @Schema(description = "Our team is playing at home", example = "true")
    private boolean home;

    @Schema(description = "Championship")
    private ChampionshipResponseDto championship;

    @Schema(description = "Match status", example = "SCHEDULED")
    private String status;

    @Schema(description = "Coach message")
    private String coachMessage;

    @Schema(description = "Team name that forfeited", example = "VC Marseille")
    private String forfeitedBy;

    @Schema(description = "Winning team name", example = "Skyball Volley")
    private String winner;

    @Schema(description = "Sets played")
    private List<MatchSetDto> sets;

    @Schema(description = "Team sets won", example = "3")
    private int teamSetsWon;

    @Schema(description = "Opponent sets won", example = "1")
    private int opponentSetsWon;

    @Schema(description = "Player attendance")
    private List<MatchPlayerDto> players;

    public static MatchResponseDto from(Match match) {
        String opponentDisplayName = match.getOpponentTeam() != null
                ? match.getOpponentTeam().getName()
                : match.getOpponentName();

        String homeTeamName = match.isHome() ? match.getTeam().getName() : opponentDisplayName;
        String awayTeamName = match.isHome() ? opponentDisplayName : match.getTeam().getName();

        int teamSetsWon = (int) match.getSets().stream().filter(s -> s.getTeamPoints() > s.getOpponentPoints()).count();
        int opponentSetsWon = (int) match.getSets().stream().filter(s -> s.getOpponentPoints() > s.getTeamPoints()).count();

        return MatchResponseDto.builder()
                .id(match.getId())
                .team(TeamSummaryDto.from(match.getTeam()))
                .opponentTeamId(match.getOpponentTeam() != null ? match.getOpponentTeam().getId() : null)
                .homeTeamName(homeTeamName)
                .awayTeamName(awayTeamName)
                .matchDate(match.getMatchDate())
                .location(match.getLocation())
                .home(match.isHome())
                .championship(match.getChampionship() != null ? ChampionshipResponseDto.from(match.getChampionship()) : null)
                .status(match.getStatus().name())
                .coachMessage(match.getCoachMessage())
                .forfeitedBy(resolveSideToName(match.getForfeitedBy(), homeTeamName, awayTeamName))
                .winner(computeWinner(match, teamSetsWon, opponentSetsWon, match.getTeam().getName(), opponentDisplayName))
                .sets(match.getSets().stream().map(MatchSetDto::from).toList())
                .teamSetsWon(teamSetsWon)
                .opponentSetsWon(opponentSetsWon)
                .players(match.getPlayers().stream().map(MatchPlayerDto::from).toList())
                .build();
    }

    private static String computeWinner(Match match, int teamSetsWon, int opponentSetsWon,
                                        String teamName, String opponentName) {
        return switch (match.getStatus()) {
            case PLAYED, FORFEIT -> {
                if (teamSetsWon >= 3) yield teamName;
                if (opponentSetsWon >= 3) yield opponentName;
                yield null;
            }
            default -> null;
        };
    }

    private static String resolveSideToName(MatchSide side, String homeTeamName, String awayTeamName) {
        if (side == null) return null;
        return side == MatchSide.HOME ? homeTeamName : awayTeamName;
    }
}
