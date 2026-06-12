package com.skyball.volley.team.persistence;

import com.skyball.volley.common.MembershipRole;
import com.skyball.volley.team.domain.TeamMembership;
import com.skyball.volley.team.domain.TeamMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, TeamMembershipId> {

    boolean existsByIdUserIdAndTeamClubIsNullAndRole(Long userId, MembershipRole role);

    @Modifying
    @Query("DELETE FROM TeamMembership tm WHERE tm.id.userId = :userId AND tm.team.club.id = :clubId")
    void deleteByUserIdAndTeamClubId(@Param("userId") Long userId, @Param("clubId") Long clubId);
}
