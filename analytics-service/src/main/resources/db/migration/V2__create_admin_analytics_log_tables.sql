CREATE TABLE IF NOT EXISTS user_location_logs (
    location_log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    zone_id BIGINT,
    zone_name VARCHAR(100),
    grid_id BIGINT,
    grid_x INTEGER,
    grid_y INTEGER,
    entered_at TIMESTAMP NOT NULL,
    exited_at TIMESTAMP,
    stay_duration_seconds BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_location_logs_store_entered_at
    ON user_location_logs (store_id, entered_at);

CREATE INDEX IF NOT EXISTS idx_user_location_logs_user_entered_at
    ON user_location_logs (user_id, entered_at);

CREATE INDEX IF NOT EXISTS idx_user_location_logs_zone
    ON user_location_logs (zone_id);


CREATE TABLE IF NOT EXISTS recommendation_logs (
    recommendation_log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    source_product_id BIGINT,
    recommendation_type VARCHAR(50) NOT NULL,
    display_position VARCHAR(100),
    is_clicked BOOLEAN NOT NULL DEFAULT FALSE,
    is_purchased BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_recommendation_logs_type_created_at
    ON recommendation_logs (recommendation_type, created_at);

CREATE INDEX IF NOT EXISTS idx_recommendation_logs_product
    ON recommendation_logs (product_id);

CREATE INDEX IF NOT EXISTS idx_recommendation_logs_source_product
    ON recommendation_logs (source_product_id);


CREATE TABLE IF NOT EXISTS route_usage_logs (
    route_usage_log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    route_mode VARCHAR(50) NOT NULL,
    duration_seconds BIGINT,
    used_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_route_usage_logs_store_used_at
    ON route_usage_logs (store_id, used_at);

CREATE INDEX IF NOT EXISTS idx_route_usage_logs_mode
    ON route_usage_logs (route_mode);


CREATE TABLE IF NOT EXISTS route_waypoint_logs (
    route_waypoint_log_id BIGSERIAL PRIMARY KEY,
    route_usage_log_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    visit_order INTEGER NOT NULL,
    visited_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_route_waypoint_logs_route_usage
    ON route_waypoint_logs (route_usage_log_id);

CREATE INDEX IF NOT EXISTS idx_route_waypoint_logs_product
    ON route_waypoint_logs (product_id);


CREATE TABLE IF NOT EXISTS daily_operation_metrics (
    daily_operation_metric_id BIGSERIAL PRIMARY KEY,
    store_id BIGINT,
    metric_date DATE NOT NULL,
    visitor_count BIGINT NOT NULL DEFAULT 0,
    route_usage_count BIGINT NOT NULL DEFAULT 0,
    recommendation_exposure_count BIGINT NOT NULL DEFAULT 0,
    recommendation_click_count BIGINT NOT NULL DEFAULT 0,
    stockout_count BIGINT NOT NULL DEFAULT 0,
    purchase_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_daily_operation_metrics_store_date UNIQUE (store_id, metric_date)
);

CREATE INDEX IF NOT EXISTS idx_daily_operation_metrics_date
    ON daily_operation_metrics (metric_date);

CREATE INDEX IF NOT EXISTS idx_daily_operation_metrics_store
    ON daily_operation_metrics (store_id);