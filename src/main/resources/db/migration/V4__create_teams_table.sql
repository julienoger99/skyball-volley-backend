CREATE TABLE teams
(
    id       BIGSERIAL   PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    category VARCHAR(10)  NOT NULL,
    gender   VARCHAR(10)  NOT NULL,
    logo_url VARCHAR(255),
    club_id  BIGINT       NOT NULL,
    CONSTRAINT fk_teams_club FOREIGN KEY (club_id) REFERENCES clubs (id)
);

CREATE TABLE team_members
(
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (team_id, user_id),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users (id)
);
