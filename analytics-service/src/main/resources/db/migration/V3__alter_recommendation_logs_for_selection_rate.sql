ALTER TABLE analytics_service.recommendation_logs
    ADD COLUMN IF NOT EXISTS product_name VARCHAR(255);

ALTER TABLE analytics_service.recommendation_logs
    ADD COLUMN IF NOT EXISTS source_product_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_recommendation_logs_source_product
    ON analytics_service.recommendation_logs (source_product_id);