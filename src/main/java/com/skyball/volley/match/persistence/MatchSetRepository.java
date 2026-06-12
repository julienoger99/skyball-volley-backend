package com.skyball.volley.match.persistence;

import com.skyball.volley.match.domain.MatchSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchSetRepository extends JpaRepository<MatchSet, Long> {
    Optional<MatchSet> findByMatchIdAndSetNumber(Long matchId, int setNumber);
    void deleteByMatchIdAndSetNumber(Long matchId, int setNumber);
    void deleteByMatchId(Long matchId);
}
