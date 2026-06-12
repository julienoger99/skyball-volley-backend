package com.skyball.volley.championship.service;

import com.skyball.volley.championship.domain.Championship;
import com.skyball.volley.championship.dto.ChampionshipResponseDto;
import com.skyball.volley.championship.dto.CreateChampionshipDto;
import com.skyball.volley.championship.dto.UpdateChampionshipDto;
import com.skyball.volley.championship.exception.ChampionshipNotFoundException;
import com.skyball.volley.championship.persistence.ChampionshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChampionshipServiceTest {

    @Mock private ChampionshipRepository championshipRepository;

    @InjectMocks private ChampionshipService championshipService;

    private static final Long ID = 1L;

    private Championship championship() {
        return Championship.builder().id(ID).name("Championnat Régional").season("2025-2026").build();
    }

    // ── getAllChampionships ────────────────────────────────────────────────────

    @Test
    void getAllChampionships_returnsList() {
        when(championshipRepository.findAll()).thenReturn(List.of(championship()));

        List<ChampionshipResponseDto> result = championshipService.getAllChampionships();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Championnat Régional");
    }

    // ── getChampionshipById ───────────────────────────────────────────────────

    @Test
    void getChampionshipById_found_returnsDto() {
        when(championshipRepository.findById(ID)).thenReturn(Optional.of(championship()));

        ChampionshipResponseDto result = championshipService.getChampionshipById(ID);

        assertThat(result.getName()).isEqualTo("Championnat Régional");
    }

    @Test
    void getChampionshipById_notFound_throws() {
        when(championshipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> championshipService.getChampionshipById(99L))
                .isInstanceOf(ChampionshipNotFoundException.class);
    }

    // ── createChampionship ────────────────────────────────────────────────────

    @Test
    void createChampionship_savesAndReturns() {
        CreateChampionshipDto dto = new CreateChampionshipDto();
        dto.setName("Championnat Régional");
        dto.setSeason("2025-2026");

        when(championshipRepository.save(any())).thenReturn(championship());

        ChampionshipResponseDto result = championshipService.createChampionship(dto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Championnat Régional");
        verify(championshipRepository).save(any());
    }

    // ── updateChampionship ────────────────────────────────────────────────────

    @Test
    void updateChampionship_found_updatesName() {
        Championship existing = championship();
        UpdateChampionshipDto dto = new UpdateChampionshipDto();
        dto.setName("Nouveau Nom");

        when(championshipRepository.findById(ID)).thenReturn(Optional.of(existing));
        when(championshipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ChampionshipResponseDto result = championshipService.updateChampionship(ID, dto);

        assertThat(result.getName()).isEqualTo("Nouveau Nom");
    }

    @Test
    void updateChampionship_notFound_throws() {
        when(championshipRepository.findById(99L)).thenReturn(Optional.empty());

        var dto = new UpdateChampionshipDto();
        assertThatThrownBy(() -> championshipService.updateChampionship(99L, dto))
                .isInstanceOf(ChampionshipNotFoundException.class);
    }

    // ── deleteChampionship ────────────────────────────────────────────────────

    @Test
    void deleteChampionship_found_deletes() {
        when(championshipRepository.existsById(ID)).thenReturn(true);

        championshipService.deleteChampionship(ID);

        verify(championshipRepository).deleteById(ID);
    }

    @Test
    void deleteChampionship_notFound_throws() {
        when(championshipRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> championshipService.deleteChampionship(99L))
                .isInstanceOf(ChampionshipNotFoundException.class);
    }
}
