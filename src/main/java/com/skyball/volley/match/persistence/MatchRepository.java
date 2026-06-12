package com.skyball.volley.match.persistence;

import com.skyball.volley.match.domain.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Page<Match> findByTeamId(Long teamId, Pageable pageable);
    Page<Match> findByChampionshipId(Long championshipId, Pageable pageable);
}
