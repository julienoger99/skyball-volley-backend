package com.skyball.volley.match.dto;

import com.skyball.volley.match.domain.MatchSide;
import com.skyball.volley.match.domain.MatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Match update request — all fields optional")
public class UpdateMatchDto {

    @Schema(description = "Match date and time")
    private LocalDateTime matchDate;

    @Schema(description = "Venue", example = "Gymnase Marcel Cerdan")
    private String location;

    @Schema(description = "Playing at home", example = "true")
    private Boolean home;

    @Schema(description = "Championship ID")
    private Long championshipId;

    @Schema(description = "Match status", example = "POSTPONED")
    private MatchStatus status;

    @Schema(description = "Side that forfeited (required when status = FORFEIT)", example = "AWAY")
    private MatchSide forfeitedBy;

    @Schema(description = "Coach message")
    private String coachMessage;
}
