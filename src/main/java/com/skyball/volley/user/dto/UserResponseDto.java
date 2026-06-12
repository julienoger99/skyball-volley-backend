package com.skyball.volley.user.dto;

import com.skyball.volley.club.dto.ClubMembershipSummaryDto;
import com.skyball.volley.team.dto.TeamMembershipSummaryDto;
import com.skyball.volley.user.domain.AppUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "User information response")
public class UserResponseDto {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "Email address", example = "john@example.com")
    private String email;

    @Schema(description = "User role", example = "PLAYER")
    private String role;

    @Schema(description = "Account creation timestamp", example = "2026-03-24T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Club memberships")
    private List<ClubMembershipSummaryDto> clubMemberships;

    @Schema(description = "Team memberships")
    private List<TeamMembershipSummaryDto> teamMemberships;

    public static UserResponseDto from(AppUser user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .createdAt(user.getCreatedAt())
                .clubMemberships(user.getClubMemberships().stream()
                        .map(ClubMembershipSummaryDto::from)
                        .toList())
                .teamMemberships(user.getTeamMemberships().stream()
                        .map(TeamMembershipSummaryDto::from)
                        .toList())
                .build();
    }

    public static UserResponseDto fromBasic(AppUser user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
