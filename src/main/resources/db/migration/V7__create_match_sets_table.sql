CREATE TABLE match_sets (
    id              BIGSERIAL PRIMARY KEY,
    match_id        BIGINT    NOT NULL,
    set_number      INT       NOT NULL,
    team_points     INT       NOT NULL,
    opponent_points INT       NOT NULL,
    CONSTRAINT fk_match_sets_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,
    CONSTRAINT uk_match_sets       UNIQUE (match_id, set_number)
);
