CREATE TABLE IF NOT EXISTS promotions (
    promotion_id        BIGINT PRIMARY KEY,
    promotion_name      VARCHAR(255)  NOT NULL,
    promotion_type      VARCHAR(50)   NOT NULL,
    min_purchase_amount INTEGER,
    buy_quantity        INTEGER,
    get_quantity        INTEGER,
    gift_item           VARCHAR(255),
    discount_rate       NUMERIC(5, 2),
    start_at            TIMESTAMP     NOT NULL,
    end_at              TIMESTAMP     NOT NULL,
    status              VARCHAR(50)   NOT NULL
    );

CREATE TABLE IF NOT EXISTS promotion_products (
    promotion_product_id BIGINT PRIMARY KEY,
    promotion_id         BIGINT      NOT NULL,
    product_id           BIGINT      NOT NULL,
    promotion_price      INTEGER,
    grid_id              BIGINT      NOT NULL,

    CONSTRAINT fk_promotion_products_promotion
    FOREIGN KEY (promotion_id)
    REFERENCES promotions (promotion_id)
    );

CREATE INDEX IF NOT EXISTS idx_promotion_products_product_id
    ON promotion_products (product_id);

CREATE INDEX IF NOT EXISTS idx_promotion_products_promotion_id
    ON promotion_products (promotion_id);

CREATE INDEX IF NOT EXISTS idx_promotions_status_period
    ON promotions (status, start_at, end_at);

CREATE UNIQUE INDEX IF NOT EXISTS uk_promotion_products_promotion_product
    ON promotion_products (promotion_id, product_id);