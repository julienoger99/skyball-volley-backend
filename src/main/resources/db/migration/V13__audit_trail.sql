ALTER TABLE clubs
    ADD COLUMN created_by VARCHAR(50),
    ADD COLUMN updated_by VARCHAR(50),
    ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE teams
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN created_by VARCHAR(50),
    ADD COLUMN updated_by VARCHAR(50),
    ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE matches
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN created_by VARCHAR(50),
    ADD COLUMN updated_by VARCHAR(50),
    ADD COLUMN updated_at TIMESTAMP;
