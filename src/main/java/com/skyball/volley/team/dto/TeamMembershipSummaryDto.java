package com.skyball.volley.team.dto;

import com.skyball.volley.team.domain.TeamMembership;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Team membership summary")
public class TeamMembershipSummaryDto {

    @Schema(description = "Team ID", example = "1")
    private Long teamId;

    @Schema(description = "Team name", example = "Équipe A")
    private String teamName;

    @Schema(description = "Role in the team", example = "MEMBER")
    private String role;

    public static TeamMembershipSummaryDto from(TeamMembership tm) {
        return TeamMembershipSummaryDto.builder()
                .teamId(tm.getTeam().getId())
                .teamName(tm.getTeam().getName())
                .role(tm.getRole().name())
                .build();
    }
}
