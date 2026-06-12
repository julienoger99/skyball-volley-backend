package com.skyball.volley.match.domain;

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
public class MatchPlayerId implements Serializable {

    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "player_id")
    private Long playerId;
}
