CREATE TABLE IF NOT EXISTS chat_sessions (
    chat_session_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    last_message VARCHAR(200),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_id_updated_at
ON chat_sessions (user_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS chat_messages (
    chat_message_id BIGSERIAL PRIMARY KEY,
    chat_session_id BIGINT NOT NULL,
    sender_type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    intent VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_chat_messages_chat_session
        FOREIGN KEY (chat_session_id)
        REFERENCES chat_sessions (chat_session_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id_created_at
ON chat_messages (chat_session_id, created_at ASC);