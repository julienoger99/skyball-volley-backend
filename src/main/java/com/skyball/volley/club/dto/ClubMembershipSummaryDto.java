package com.skyball.volley.club.dto;

import com.skyball.volley.club.domain.ClubMembership;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Club membership summary")
public class ClubMembershipSummaryDto {

    @Schema(description = "Club ID", example = "1")
    private Long clubId;

    @Schema(description = "Club name", example = "Skyball Volley Club")
    private String clubName;

    @Schema(description = "Role in the club", example = "MEMBER")
    private String role;

    public static ClubMembershipSummaryDto from(ClubMembership cm) {
        return ClubMembershipSummaryDto.builder()
                .clubId(cm.getClub().getId())
                .clubName(cm.getClub().getName())
                .role(cm.getRole().name())
                .build();
    }
}
