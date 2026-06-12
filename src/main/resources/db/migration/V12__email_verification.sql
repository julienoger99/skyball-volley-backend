ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE email_verification_tokens (
    token      VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE
);

UPDATE users SET email_verified = TRUE WHERE super_admin = TRUE;
