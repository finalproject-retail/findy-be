CREATE TABLE IF NOT EXISTS chatbot_logs (
    chatbot_log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    chat_session_id BIGINT,
    intent VARCHAR(50),
    keyword VARCHAR(255),
    request_message TEXT NOT NULL,
    response_message TEXT,
    status VARCHAR(20) NOT NULL,
    failure_reason TEXT,
    duration_ms BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chatbot_logs_created_at
ON chatbot_logs (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_chatbot_logs_status_created_at
ON chatbot_logs (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_chatbot_logs_intent_created_at
ON chatbot_logs (intent, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_chatbot_logs_user_id_created_at
ON chatbot_logs (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_chatbot_logs_request_message
ON chatbot_logs (request_message);