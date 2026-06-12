package com.skyball.volley.club.persistence;

import com.skyball.volley.club.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {

    Optional<Club> findByName(String name);

    boolean existsByName(String name);
}
