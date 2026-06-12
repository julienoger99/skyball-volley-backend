package com.skyball.volley.team.controller;

import com.skyball.volley.auth.dto.ErrorResponseDto;
import com.skyball.volley.common.UpdateMemberRoleDto;
import com.skyball.volley.team.dto.CreateTeamDto;
import com.skyball.volley.team.dto.TeamResponseDto;
import com.skyball.volley.team.dto.UpdateTeamDto;
import com.skyball.volley.team.service.TeamService;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.api.base-path}/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @Operation(summary = "Get all teams")
    @ApiResponse(responseCode = "200", description = "List of teams returned successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class)))
    public ResponseEntity<Page<TeamResponseDto>> getAllTeams(@ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(teamService.getAllTeams(pageable));
    }

    @GetMapping("/club/{clubId}")
    @Operation(summary = "Get teams by club")
    @ApiResponse(responseCode = "200", description = "Teams returned successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Club not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<Page<TeamResponseDto>> getTeamsByClub(@PathVariable Long clubId, @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(teamService.getTeamsByClub(clubId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team by ID")
    @ApiResponse(responseCode = "200", description = "Team found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Team not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<TeamResponseDto> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @PostMapping
    @Operation(summary = "Create a team")
    @ApiResponse(responseCode = "201", description = "Team created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error or invalid category/gender combination",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Club not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<TeamResponseDto> createTeam(@Valid @RequestBody CreateTeamDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a team", description = "All fields are optional.")
    @ApiResponse(responseCode = "200", description = "Team updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error or invalid category/gender combination",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Team not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<TeamResponseDto> updateTeam(@PathVariable Long id, @Valid @RequestBody UpdateTeamDto dto) {
        return ResponseEntity.ok(teamService.updateTeam(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a team")
    @ApiResponse(responseCode = "204", description = "Team deleted successfully")
    @ApiResponse(responseCode = "404", description = "Team not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/members/{userId}")
    @Operation(
        summary = "Join a team",
        description = "A user can join a team with their own userId (self-join). A team manager can also add any eligible user.\n\n" +
            "Club rules:\n" +
            "- Team in a club + user already in that club → joins directly.\n" +
            "- Team in a club + user has no club → automatically joins the club too.\n" +
            "- Team in a club + user is in a different club → rejected (400).\n" +
            "- Independent team (no club) → any user can join."
    )
    @ApiResponse(responseCode = "200", description = "User joined the team (and possibly the club automatically)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "User belongs to a different club",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Cannot add another user unless you are a team manager",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Team or user not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<TeamResponseDto> addMember(@PathVariable Long teamId, @PathVariable Long userId) {
        return ResponseEntity.ok(teamService.addMember(teamId, userId));
    }

    @PatchMapping("/{teamId}/members/{userId}/role")
    @Operation(summary = "Update a team member's role")
    @ApiResponse(responseCode = "200", description = "Role updated",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "User is not a member of this team",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Team or user not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<TeamResponseDto> updateMemberRole(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleDto dto) {
        return ResponseEntity.ok(teamService.updateMemberRole(teamId, userId, dto));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @Operation(
        summary = "Leave a team",
        description = "A user can leave a team with their own userId. A team manager can also remove another member. " +
            "Note: leaving a team does not remove the user from the club."
    )
    @ApiResponse(responseCode = "200", description = "User left the team",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Cannot remove another user unless you are a team manager",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Team or user not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<TeamResponseDto> removeMember(@PathVariable Long teamId, @PathVariable Long userId) {
        return ResponseEntity.ok(teamService.removeMember(teamId, userId));
    }
}
