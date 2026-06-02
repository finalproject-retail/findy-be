CREATE TABLE IF NOT EXISTS recent_view_products (
    recent_view_id BIGSERIAL PRIMARY KEY,
    user_id        BIGINT    NOT NULL,
    product_id     BIGINT    NOT NULL REFERENCES products (product_id),
    viewed_at      TIMESTAMP NOT NULL,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL,

    CONSTRAINT uk_recent_view_products_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_recent_view_products_user_viewed_at
    ON recent_view_products (user_id, viewed_at DESC);

CREATE INDEX IF NOT EXISTS idx_recent_view_products_product_id
    ON recent_view_products (product_id);