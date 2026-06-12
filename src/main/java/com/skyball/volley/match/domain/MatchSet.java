package com.skyball.volley.match.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "match_sets")
public class MatchSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "set_number", nullable = false)
    private int setNumber;

    @Column(name = "team_points", nullable = false)
    private int teamPoints;

    @Column(name = "opponent_points", nullable = false)
    private int opponentPoints;
}
