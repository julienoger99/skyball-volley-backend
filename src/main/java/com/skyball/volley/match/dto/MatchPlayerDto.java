package com.skyball.volley.match.dto;

import com.skyball.volley.match.domain.MatchPlayer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Player attendance on a match")
public class MatchPlayerDto {

    @Schema(description = "Player ID", example = "1")
    private Long playerId;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "Attendance status", example = "PRESENT")
    private String attendanceStatus;

    @Schema(description = "Whether this player is the team captain for this match", example = "false")
    private boolean captain;

    public static MatchPlayerDto from(MatchPlayer mp) {
        return MatchPlayerDto.builder()
                .playerId(mp.getPlayer().getId())
                .username(mp.getPlayer().getUsername())
                .attendanceStatus(mp.getAttendanceStatus().name())
                .captain(mp.isCaptain())
                .build();
    }
}
