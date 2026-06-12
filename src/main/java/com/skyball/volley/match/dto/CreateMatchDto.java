package com.skyball.volley.match.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Match creation request")
public class CreateMatchDto {

    @Schema(description = "Opponent team ID if registered in the system", example = "2")
    private Long opponentTeamId;

    @Schema(description = "Opponent name if not registered in the system", example = "VC Marseille")
    private String opponentName;

    @NotNull(message = "Match date is required")
    @Future(message = "Match date must be in the future")
    @Schema(description = "Match date and time", example = "2026-05-15T14:00:00")
    private LocalDateTime matchDate;

    @Schema(description = "Venue", example = "Gymnase Marcel Cerdan")
    private String location;

    @Schema(description = "Playing at home", example = "true")
    private boolean home;

    @Schema(description = "Championship ID (omit for friendly matches)", example = "1")
    private Long championshipId;

    @Schema(description = "Coach message for this match")
    private String coachMessage;
}
