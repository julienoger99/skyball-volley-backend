package com.skyball.volley.team.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TeamMembershipId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "team_id")
    private Long teamId;
}
