package com.skyball.volley.championship.service;

import com.skyball.volley.championship.domain.Championship;
import com.skyball.volley.championship.dto.ChampionshipResponseDto;
import com.skyball.volley.championship.dto.CreateChampionshipDto;
import com.skyball.volley.championship.dto.UpdateChampionshipDto;
import com.skyball.volley.championship.exception.ChampionshipNotFoundException;
import com.skyball.volley.championship.persistence.ChampionshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChampionshipService {

    private final ChampionshipRepository championshipRepository;

    @Transactional(readOnly = true)
    public List<ChampionshipResponseDto> getAllChampionships() {
        return championshipRepository.findAll().stream()
                .map(ChampionshipResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChampionshipResponseDto getChampionshipById(Long id) {
        return ChampionshipResponseDto.from(findOrThrow(id));
    }

    @PreAuthorize("@sec.isSuperAdmin()")
    @Transactional
    public ChampionshipResponseDto createChampionship(CreateChampionshipDto dto) {
        Championship championship = Championship.builder()
                .name(dto.getName())
                .season(dto.getSeason())
                .category(dto.getCategory())
                .build();
        return ChampionshipResponseDto.from(championshipRepository.save(championship));
    }

    @PreAuthorize("@sec.isSuperAdmin()")
    @Transactional
    public ChampionshipResponseDto updateChampionship(Long id, UpdateChampionshipDto dto) {
        Championship championship = findOrThrow(id);
        if (dto.getName() != null) championship.setName(dto.getName());
        if (dto.getSeason() != null) championship.setSeason(dto.getSeason());
        if (dto.getCategory() != null) championship.setCategory(dto.getCategory());
        return ChampionshipResponseDto.from(championshipRepository.save(championship));
    }

    @PreAuthorize("@sec.isSuperAdmin()")
    @Transactional
    public void deleteChampionship(Long id) {
        if (!championshipRepository.existsById(id)) {
            throw new ChampionshipNotFoundException(id);
        }
        championshipRepository.deleteById(id);
    }

    private Championship findOrThrow(Long id) {
        return championshipRepository.findById(id)
                .orElseThrow(() -> new ChampionshipNotFoundException(id));
    }
}
