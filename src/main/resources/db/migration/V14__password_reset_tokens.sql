CREATE TABLE password_reset_tokens (
    token      VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE
);
