CREATE TABLE IF NOT EXISTS product_view_analytics (
    product_view_analytics_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    category_id BIGINT,
    category_name VARCHAR(100),
    view_count BIGINT NOT NULL DEFAULT 0,
    analysis_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_product_view_analytics_product_date UNIQUE (product_id, analysis_date)
);

CREATE INDEX IF NOT EXISTS idx_product_view_analytics_date
    ON product_view_analytics (analysis_date);

CREATE INDEX IF NOT EXISTS idx_product_view_analytics_product
    ON product_view_analytics (product_id);


CREATE TABLE IF NOT EXISTS shopping_list_product_analytics (
    shopping_list_product_analytics_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    category_id BIGINT,
    category_name VARCHAR(100),
    added_count BIGINT NOT NULL DEFAULT 0,
    total_quantity BIGINT NOT NULL DEFAULT 0,
    analysis_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_shopping_list_product_analytics_product_date UNIQUE (product_id, analysis_date)
);

CREATE INDEX IF NOT EXISTS idx_shopping_list_product_analytics_date
    ON shopping_list_product_analytics (analysis_date);

CREATE INDEX IF NOT EXISTS idx_shopping_list_product_analytics_product
    ON shopping_list_product_analytics (product_id);


CREATE TABLE IF NOT EXISTS recommendation_click_rate_analytics (
    recommendation_click_rate_analytics_id BIGSERIAL PRIMARY KEY,
    recommendation_type VARCHAR(50) NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(255),
    impression_count BIGINT NOT NULL DEFAULT 0,
    click_count BIGINT NOT NULL DEFAULT 0,
    click_rate NUMERIC(6, 2) NOT NULL DEFAULT 0,
    analysis_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_recommendation_click_rate_analytics UNIQUE (
        recommendation_type,
        product_id,
        analysis_date
    )
);

CREATE INDEX IF NOT EXISTS idx_recommendation_click_rate_analytics_date
    ON recommendation_click_rate_analytics (analysis_date);

CREATE INDEX IF NOT EXISTS idx_recommendation_click_rate_analytics_type
    ON recommendation_click_rate_analytics (recommendation_type);


CREATE TABLE IF NOT EXISTS recommendation_conversion_analytics (
    recommendation_conversion_analytics_id BIGSERIAL PRIMARY KEY,
    recommendation_type VARCHAR(50) NOT NULL,
    product_id BIGINT,
    product_name VARCHAR(255),
    click_count BIGINT NOT NULL DEFAULT 0,
    purchase_count BIGINT NOT NULL DEFAULT 0,
    conversion_rate NUMERIC(6, 2) NOT NULL DEFAULT 0,
    analysis_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_recommendation_conversion_analytics UNIQUE (
        recommendation_type,
        product_id,
        analysis_date
    )
);

CREATE INDEX IF NOT EXISTS idx_recommendation_conversion_analytics_date
    ON recommendation_conversion_analytics (analysis_date);

CREATE INDEX IF NOT EXISTS idx_recommendation_conversion_analytics_type
    ON recommendation_conversion_analytics (recommendation_type);


CREATE TABLE IF NOT EXISTS promotion_sell_through_analytics (
    promotion_sell_through_analytics_id BIGSERIAL PRIMARY KEY,
    promotion_id BIGINT NOT NULL,
    promotion_name VARCHAR(255),
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    initial_stock_quantity INTEGER NOT NULL DEFAULT 0,
    sold_quantity INTEGER NOT NULL DEFAULT 0,
    remaining_stock_quantity INTEGER NOT NULL DEFAULT 0,
    sell_through_rate NUMERIC(6, 2) NOT NULL DEFAULT 0,
    analysis_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_promotion_sell_through_analytics UNIQUE (
        promotion_id,
        product_id,
        analysis_date
    )
);

CREATE INDEX IF NOT EXISTS idx_promotion_sell_through_analytics_date
    ON promotion_sell_through_analytics (analysis_date);

CREATE INDEX IF NOT EXISTS idx_promotion_sell_through_analytics_promotion
    ON promotion_sell_through_analytics (promotion_id);


CREATE TABLE IF NOT EXISTS stockout_analytics (
    stockout_analytics_id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    category_id BIGINT,
    category_name VARCHAR(100),
    store_id BIGINT,
    stockout_count BIGINT NOT NULL DEFAULT 0,
    low_stock_count BIGINT NOT NULL DEFAULT 0,
    analysis_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_stockout_analytics_product_store_date UNIQUE (
        product_id,
        store_id,
        analysis_date
    )
);

CREATE INDEX IF NOT EXISTS idx_stockout_analytics_date
    ON stockout_analytics (analysis_date);

CREATE INDEX IF NOT EXISTS idx_stockout_analytics_product
    ON stockout_analytics (product_id);

CREATE INDEX IF NOT EXISTS idx_stockout_analytics_store
    ON stockout_analytics (store_id);


CREATE TABLE IF NOT EXISTS cross_sell_analytics (
    cross_sell_analytics_id BIGSERIAL PRIMARY KEY,
    base_product_id BIGINT NOT NULL,
    base_product_name VARCHAR(255),
    related_product_id BIGINT NOT NULL,
    related_product_name VARCHAR(255),
    order_count BIGINT NOT NULL DEFAULT 0,
    support_rate NUMERIC(6, 2) NOT NULL DEFAULT 0,
    analysis_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_cross_sell_analytics_product_pair_date UNIQUE (
        base_product_id,
        related_product_id,
        analysis_date
    )
);

CREATE INDEX IF NOT EXISTS idx_cross_sell_analytics_date
    ON cross_sell_analytics (analysis_date);

CREATE INDEX IF NOT EXISTS idx_cross_sell_analytics_base_product
    ON cross_sell_analytics (base_product_id);

CREATE INDEX IF NOT EXISTS idx_cross_sell_analytics_related_product
    ON cross_sell_analytics (related_product_id);