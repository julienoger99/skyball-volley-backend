package com.skyball.volley.club.dto;

import com.skyball.volley.club.domain.ClubMembership;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Club member (user + role within the club)")
public class ClubMemberDto {

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Username", example = "john.doe")
    private String username;

    @Schema(description = "Role in the club", example = "MEMBER")
    private String role;

    public static ClubMemberDto from(ClubMembership cm) {
        return ClubMemberDto.builder()
                .userId(cm.getUser().getId())
                .username(cm.getUser().getUsername())
                .role(cm.getRole().name())
                .build();
    }
}
