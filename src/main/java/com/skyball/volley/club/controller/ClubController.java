package com.skyball.volley.club.controller;

import com.skyball.volley.auth.dto.ErrorResponseDto;
import com.skyball.volley.club.dto.ClubMemberDto;
import com.skyball.volley.club.dto.ClubResponseDto;
import com.skyball.volley.club.dto.CreateClubDto;
import com.skyball.volley.club.dto.UpdateClubDto;
import com.skyball.volley.club.service.ClubService;
import com.skyball.volley.common.UpdateMemberRoleDto;
import com.skyball.volley.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/clubs")
@RequiredArgsConstructor
@Tag(name = "Clubs", description = "Club management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ClubController {

    private final ClubService clubService;

    @GetMapping
    @Operation(summary = "Get all clubs")
    @ApiResponse(responseCode = "200", description = "List of clubs returned successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubResponseDto.class)))
    public ResponseEntity<Page<ClubResponseDto>> getAllClubs(@ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(clubService.getAllClubs(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get club by ID")
    @ApiResponse(responseCode = "200", description = "Club found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Club not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<ClubResponseDto> getClubById(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getClubById(id));
    }

    @PostMapping
    @Operation(summary = "Create a club")
    @ApiResponse(responseCode = "201", description = "Club created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "409", description = "Club name already exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<ClubResponseDto> createClub(@Valid @RequestBody CreateClubDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clubService.createClub(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a club", description = "All fields are optional.")
    @ApiResponse(responseCode = "200", description = "Club updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Club not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "409", description = "Club name already taken",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<ClubResponseDto> updateClub(@PathVariable Long id, @Valid @RequestBody UpdateClubDto dto) {
        return ResponseEntity.ok(clubService.updateClub(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a club")
    @ApiResponse(responseCode = "204", description = "Club deleted successfully")
    @ApiResponse(responseCode = "404", description = "Club not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clubId}/members")
    @Operation(summary = "Get club members", description = "Returns the members of a club with their role. Caller must be a member of the club.")
    @ApiResponse(responseCode = "200", description = "Club members returned successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMemberDto.class)))
    @ApiResponse(responseCode = "403", description = "Caller is not a member of this club",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Club not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<List<ClubMemberDto>> getClubMembers(@PathVariable Long clubId) {
        return ResponseEntity.ok(clubService.getClubMembers(clubId));
    }

    @PostMapping("/{clubId}/members/{userId}")
    @Operation(
        summary = "Join a club",
        description = "A user can join a club with their own userId (self-join). A club admin can also add another user. " +
            "A user can only belong to one club at a time."
    )
    @ApiResponse(responseCode = "200", description = "User joined the club",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Cannot add another user unless you are a club admin",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Club or user not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "409", description = "User already belongs to a club",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<UserResponseDto> joinClub(@PathVariable Long clubId, @PathVariable Long userId) {
        return ResponseEntity.ok(clubService.joinClub(clubId, userId));
    }

    @PatchMapping("/{clubId}/members/{userId}/role")
    @Operation(summary = "Update a club member's role")
    @ApiResponse(responseCode = "200", description = "Role updated",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "User is not a member of this club",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Club or user not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<UserResponseDto> updateMemberRole(
            @PathVariable Long clubId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleDto dto) {
        return ResponseEntity.ok(clubService.updateMemberRole(clubId, userId, dto));
    }

    @DeleteMapping("/{clubId}/members/{userId}")
    @Operation(
        summary = "Leave a club",
        description = "A user can leave a club with their own userId. A club admin can also remove another member. " +
            "Leaving a club automatically removes the user from all teams of that club."
    )
    @ApiResponse(responseCode = "204", description = "User left the club")
    @ApiResponse(responseCode = "400", description = "User is not a member of this club",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Cannot remove another user unless you are a club admin",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Club or user not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<Void> leaveClub(@PathVariable Long clubId, @PathVariable Long userId) {
        clubService.leaveClub(clubId, userId);
        return ResponseEntity.noContent().build();
    }
}
