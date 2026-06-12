package com.skyball.volley.match.dto;

import com.skyball.volley.match.domain.MatchSet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Set score")
public class MatchSetDto {

    @Schema(description = "Set number", example = "1")
    private int setNumber;

    @Schema(description = "Team points", example = "25")
    private int teamPoints;

    @Schema(description = "Opponent points", example = "18")
    private int opponentPoints;

    public static MatchSetDto from(MatchSet s) {
        return MatchSetDto.builder()
                .setNumber(s.getSetNumber())
                .teamPoints(s.getTeamPoints())
                .opponentPoints(s.getOpponentPoints())
                .build();
    }
}
