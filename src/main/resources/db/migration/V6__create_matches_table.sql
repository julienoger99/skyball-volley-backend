CREATE TABLE matches (
    id                 BIGSERIAL    PRIMARY KEY,
    team_id            BIGINT       NOT NULL,
    opponent_team_id   BIGINT,
    opponent_name      VARCHAR(255),
    match_date         TIMESTAMP    NOT NULL,
    location           VARCHAR(255),
    home               BOOLEAN      NOT NULL DEFAULT FALSE,
    championship_id    BIGINT,
    status             VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    forfeited_by       VARCHAR(10),
    coach_message      TEXT,
    CONSTRAINT fk_matches_team             FOREIGN KEY (team_id)           REFERENCES teams(id),
    CONSTRAINT fk_matches_opponent_team    FOREIGN KEY (opponent_team_id)  REFERENCES teams(id),
    CONSTRAINT fk_matches_championship     FOREIGN KEY (championship_id)   REFERENCES championships(id),
    CONSTRAINT chk_matches_opponent        CHECK (opponent_team_id IS NOT NULL OR opponent_name IS NOT NULL)
);
