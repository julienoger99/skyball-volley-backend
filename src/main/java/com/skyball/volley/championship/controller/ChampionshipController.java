package com.skyball.volley.championship.controller;

import com.skyball.volley.auth.dto.ErrorResponseDto;
import com.skyball.volley.championship.dto.ChampionshipResponseDto;
import com.skyball.volley.championship.dto.CreateChampionshipDto;
import com.skyball.volley.championship.dto.UpdateChampionshipDto;
import com.skyball.volley.championship.service.ChampionshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/championships")
@RequiredArgsConstructor
@Tag(name = "Championships", description = "Championship management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ChampionshipController {

    private final ChampionshipService championshipService;

    @GetMapping
    @Operation(summary = "Get all championships")
    @ApiResponse(responseCode = "200", description = "List returned successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChampionshipResponseDto.class)))
    public ResponseEntity<List<ChampionshipResponseDto>> getAllChampionships() {
        return ResponseEntity.ok(championshipService.getAllChampionships());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get championship by ID")
    @ApiResponse(responseCode = "200", description = "Championship found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChampionshipResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Championship not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<ChampionshipResponseDto> getChampionshipById(@PathVariable Long id) {
        return ResponseEntity.ok(championshipService.getChampionshipById(id));
    }

    @PostMapping
    @Operation(summary = "Create a championship")
    @ApiResponse(responseCode = "201", description = "Championship created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChampionshipResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<ChampionshipResponseDto> createChampionship(@Valid @RequestBody CreateChampionshipDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(championshipService.createChampionship(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a championship")
    @ApiResponse(responseCode = "200", description = "Championship updated",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChampionshipResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Championship not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<ChampionshipResponseDto> updateChampionship(@PathVariable Long id, @Valid @RequestBody UpdateChampionshipDto dto) {
        return ResponseEntity.ok(championshipService.updateChampionship(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a championship")
    @ApiResponse(responseCode = "204", description = "Championship deleted")
    @ApiResponse(responseCode = "404", description = "Championship not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    public ResponseEntity<Void> deleteChampionship(@PathVariable Long id) {
        championshipService.deleteChampionship(id);
        return ResponseEntity.noContent().build();
    }
}
