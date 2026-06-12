package com.skyball.volley.championship.domain;

import com.skyball.volley.team.domain.TeamCategory;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "championships")
public class Championship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 20)
    private String season;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TeamCategory category;
}
