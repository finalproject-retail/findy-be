CREATE TABLE social_accounts
(
    social_account_id BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES users (user_id),
    provider          VARCHAR(30)  NOT NULL,
    provider_user_id  VARCHAR(255) NOT NULL,
    provider_email    VARCHAR(255),
    is_connected      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,
    CONSTRAINT uk_social_accounts_provider_user UNIQUE (provider, provider_user_id)
);

CREATE INDEX idx_social_accounts_user_id ON social_accounts (user_id);

CREATE TABLE email_verification_logs
(
    email_verification_log_id BIGSERIAL PRIMARY KEY,
    email                     VARCHAR(255) NOT NULL,
    purpose                   VARCHAR(50)  NOT NULL,
    sent_at                   TIMESTAMP    NOT NULL,
    verified_at               TIMESTAMP,
    created_at                TIMESTAMP    NOT NULL,
    updated_at                TIMESTAMP    NOT NULL
);

CREATE INDEX idx_email_verification_logs_email_purpose ON email_verification_logs (email, purpose);
