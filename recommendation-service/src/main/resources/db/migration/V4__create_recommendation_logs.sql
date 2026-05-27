CREATE TABLE IF NOT EXISTS recommendation_logs (
    recommendation_log_id BIGSERIAL PRIMARY KEY,
    user_id               BIGINT       NOT NULL,
    product_id            BIGINT       NOT NULL,
    source_product_id     BIGINT,
    store_id              BIGINT,
    recommendation_type   VARCHAR(50)  NOT NULL,
    log_type              VARCHAR(50)  NOT NULL,
    display_location      VARCHAR(100) NOT NULL,
    recommendation_rank   INTEGER,
    score                 NUMERIC(8, 3),
    reason                VARCHAR(500),
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_recommendation_logs_user_id_created_at
    ON recommendation_logs (user_id, created_at);

CREATE INDEX IF NOT EXISTS idx_recommendation_logs_product_id
    ON recommendation_logs (product_id);

CREATE INDEX IF NOT EXISTS idx_recommendation_logs_type_created_at
    ON recommendation_logs (recommendation_type, log_type, created_at);