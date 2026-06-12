package com.skyball.volley.match.persistence;

import com.skyball.volley.match.domain.MatchPlayer;
import com.skyball.volley.match.domain.MatchPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, MatchPlayerId> {
}
