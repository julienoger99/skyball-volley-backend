package com.skyball.volley.championship.persistence;

import com.skyball.volley.championship.domain.Championship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChampionshipRepository extends JpaRepository<Championship, Long> {
}
