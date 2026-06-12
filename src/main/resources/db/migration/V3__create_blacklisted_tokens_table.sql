CREATE TABLE blacklisted_tokens
(
    id         BIGSERIAL    PRIMARY KEY,
    jti        VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    CONSTRAINT uk_blacklisted_tokens_jti UNIQUE (jti)
);
