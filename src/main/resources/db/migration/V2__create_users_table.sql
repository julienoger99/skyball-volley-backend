CREATE TABLE users
(
    id         BIGSERIAL    PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP,
    club_id    BIGINT,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_club FOREIGN KEY (club_id) REFERENCES clubs (id)
);
