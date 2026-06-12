package com.skyball.volley.match.controller;

import com.skyball.volley.auth.dto.ErrorResponseDto;
import com.skyball.volley.match.dto.*;

import com.skyball.volley.match.service.MatchService;
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
@RequestMapping("${app.api.base-path}")
@RequiredArgsConstructor
@Tag(name = "Matches", description = "Match management APIs")
@SecurityRequirement(name = "bearerAuth")
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/teams/{teamId}/matches")
    @Operation(summary = "Get all matches for a team")
    @ApiResponse(responseCode = "200", description = "Matches returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Team not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<Page<MatchResponseDto>> getMatchesByTeam(@PathVariable Long teamId, @ParameterObject @PageableDefault(size = 20, sort = "matchDate") Pageable pageable) {
        return ResponseEntity.ok(matchService.getMatchesByTeam(teamId, pageable));
    }

    @PostMapping("/teams/{teamId}/matches")
    @Operation(summary = "Create a match for a team")
    @ApiResponse(responseCode = "201", description = "Match created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Team or championship not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<MatchResponseDto> createMatch(@PathVariable Long teamId, @Valid @RequestBody CreateMatchDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(teamId, dto));
    }

    @GetMapping("/championships/{championshipId}/matches")
    @Operation(summary = "Get all matches for a championship")
    @ApiResponse(responseCode = "200", description = "Matches returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Championship not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<Page<MatchResponseDto>> getMatchesByChampionship(@PathVariable Long championshipId, @ParameterObject @PageableDefault(size = 20, sort = "matchDate") Pageable pageable) {
        return ResponseEntity.ok(matchService.getMatchesByChampionship(championshipId, pageable));
    }

    @GetMapping("/matches/{id}")
    @Operation(summary = "Get match by ID")
    @ApiResponse(responseCode = "200", description = "Match found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Match not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<MatchResponseDto> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @PutMapping("/matches/{id}")
    @Operation(summary = "Update a match")
    @ApiResponse(responseCode = "200", description = "Match updated",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Match not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<MatchResponseDto> updateMatch(@PathVariable Long id, @Valid @RequestBody UpdateMatchDto dto) {
        return ResponseEntity.ok(matchService.updateMatch(id, dto));
    }

    @DeleteMapping("/matches/{id}")
    @Operation(summary = "Delete a match")
    @ApiResponse(responseCode = "204", description = "Match deleted")
    @ApiResponse(responseCode = "404", description = "Match not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/matches/{matchId}/sets/bulk")
    @Operation(summary = "Submit full match score", description = "Replaces all existing sets at once. Use this for post-match score entry.")
    @ApiResponse(responseCode = "200", description = "Score recorded, match returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Match not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<MatchResponseDto> setAllSets(@PathVariable Long matchId, @Valid @RequestBody BulkMatchSetsDto dto) {
        return ResponseEntity.ok(matchService.setAllSets(matchId, dto));
    }

    @PutMapping("/matches/{matchId}/sets")
    @Operation(summary = "Add or update a set score")
    @ApiResponse(responseCode = "200", description = "Set recorded, match returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Match not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<MatchResponseDto> addOrUpdateSet(@PathVariable Long matchId, @Valid @RequestBody CreateMatchSetDto dto) {
        return ResponseEntity.ok(matchService.addOrUpdateSet(matchId, dto));
    }

    @DeleteMapping("/matches/{matchId}/sets/{setNumber}")
    @Operation(summary = "Delete a set")
    @ApiResponse(responseCode = "200", description = "Set deleted, match returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Match not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<MatchResponseDto> deleteSet(@PathVariable Long matchId, @PathVariable int setNumber) {
        return ResponseEntity.ok(matchService.deleteSet(matchId, setNumber));
    }

    @PutMapping("/matches/{matchId}/captain/{userId}")
    @Operation(summary = "Set match captain", description = "Designates a player as captain for this match. Removes the captain flag from any previous captain.")
    @ApiResponse(responseCode = "200", description = "Captain set, match returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Match or player not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<MatchResponseDto> setCaptain(@PathVariable Long matchId, @PathVariable Long userId) {
        return ResponseEntity.ok(matchService.setCaptain(matchId, userId));
    }

    @PutMapping("/matches/{matchId}/players/{userId}/attendance")
    @Operation(summary = "Update player attendance")
    @ApiResponse(responseCode = "200", description = "Attendance updated, match returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Match or player not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<MatchResponseDto> updateAttendance(
            @PathVariable Long matchId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateAttendanceDto dto) {
        return ResponseEntity.ok(matchService.updateAttendance(matchId, userId, dto));
    }
}
