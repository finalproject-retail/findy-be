ALTER TABLE recommendation_logs
    ADD COLUMN IF NOT EXISTS source_recommendation_log_id BIGINT,
    ADD COLUMN IF NOT EXISTS order_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_recommendation_logs_source_log_id
    ON recommendation_logs (source_recommendation_log_id);

CREATE INDEX IF NOT EXISTS idx_recommendation_logs_order_id
    ON recommendation_logs (order_id);