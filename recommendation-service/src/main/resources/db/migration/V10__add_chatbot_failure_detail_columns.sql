ALTER TABLE chatbot_logs
    ADD COLUMN IF NOT EXISTS failure_type     VARCHAR(50),
    ADD COLUMN IF NOT EXISTS fallback_message TEXT;

CREATE INDEX IF NOT EXISTS idx_chatbot_logs_failure_type_created_at
    ON chatbot_logs (failure_type, created_at DESC);