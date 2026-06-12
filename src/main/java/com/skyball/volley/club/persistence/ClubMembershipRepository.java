package com.skyball.volley.club.persistence;

import com.skyball.volley.club.domain.ClubMembership;
import com.skyball.volley.club.domain.ClubMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, ClubMembershipId> {

    boolean existsByIdUserId(Long userId);

    List<ClubMembership> findByClubId(Long clubId);
}
