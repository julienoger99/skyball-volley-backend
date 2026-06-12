CREATE TABLE match_players (
    match_id          BIGINT      NOT NULL,
    player_id         BIGINT      NOT NULL,
    attendance_status VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    PRIMARY KEY (match_id, player_id),
    CONSTRAINT fk_match_players_match  FOREIGN KEY (match_id)  REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_players_player FOREIGN KEY (player_id) REFERENCES users(id)   ON DELETE CASCADE
);
