CREATE TABLE championships (
    id       BIGSERIAL    PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    season   VARCHAR(20)  NOT NULL,
    category VARCHAR(10)
);
