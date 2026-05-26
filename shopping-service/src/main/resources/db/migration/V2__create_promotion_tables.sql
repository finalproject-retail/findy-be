CREATE TABLE IF NOT EXISTS promotions (
                                          promotion_id        BIGSERIAL PRIMARY KEY,
                                          promotion_name      VARCHAR(255)  NOT NULL,
    promotion_type      VARCHAR(30)   NOT NULL,
    min_purchase_amount INTEGER,
    buy_quantity        INTEGER,
    get_quantity        INTEGER,
    gift_item           VARCHAR(255),
    discount_rate       NUMERIC(5, 2),
    start_at            TIMESTAMP     NOT NULL,
    end_at              TIMESTAMP     NOT NULL,
    status              VARCHAR(30)   NOT NULL,
    created_at          TIMESTAMP     NOT NULL,
    updated_at          TIMESTAMP     NOT NULL
    );

CREATE TABLE IF NOT EXISTS promotion_products (
                                                  promotion_product_id BIGSERIAL PRIMARY KEY,
                                                  promotion_id         BIGINT      NOT NULL REFERENCES promotions (promotion_id),
    product_id           BIGINT      NOT NULL REFERENCES products (product_id),
    promotion_price      INTEGER,
    grid_id              BIGINT      NOT NULL,
    created_at           TIMESTAMP   NOT NULL,
    updated_at           TIMESTAMP   NOT NULL,

    CONSTRAINT uk_promotion_product UNIQUE (promotion_id, product_id)
    );

CREATE INDEX IF NOT EXISTS idx_promotion_products_promotion_id
    ON promotion_products (promotion_id);

CREATE INDEX IF NOT EXISTS idx_promotion_products_product_id
    ON promotion_products (product_id);

CREATE INDEX IF NOT EXISTS idx_promotions_status_period
    ON promotions (status, start_at, end_at);