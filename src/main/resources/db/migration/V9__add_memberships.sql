ALTER TABLE users DROP CONSTRAINT fk_users_club;
ALTER TABLE users DROP COLUMN club_id;

DROP TABLE team_members;

CREATE TABLE club_memberships
(
    user_id BIGINT      NOT NULL,
    club_id BIGINT      NOT NULL,
    role    VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, club_id),
    CONSTRAINT uk_club_memberships_user UNIQUE (user_id),
    CONSTRAINT fk_club_memberships_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_club_memberships_club FOREIGN KEY (club_id) REFERENCES clubs (id) ON DELETE CASCADE
);

CREATE TABLE team_memberships
(
    user_id BIGINT      NOT NULL,
    team_id BIGINT      NOT NULL,
    role    VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, team_id),
    CONSTRAINT fk_team_memberships_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_team_memberships_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE
);

ALTER TABLE match_players ADD COLUMN captain BOOLEAN NOT NULL DEFAULT FALSE;
