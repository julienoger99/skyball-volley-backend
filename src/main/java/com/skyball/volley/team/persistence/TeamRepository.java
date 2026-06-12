package com.skyball.volley.team.persistence;

import com.skyball.volley.team.domain.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Page<Team> findByClubId(Long clubId, Pageable pageable);
    List<Team> findByClubId(Long clubId);
}
