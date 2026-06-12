CREATE TABLE clubs
(
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    city        VARCHAR(255) NOT NULL,
    logo_url    VARCHAR(255),
    description TEXT,
    website_url VARCHAR(255),
    created_at  DATE,
    CONSTRAINT uk_clubs_name UNIQUE (name)
);
