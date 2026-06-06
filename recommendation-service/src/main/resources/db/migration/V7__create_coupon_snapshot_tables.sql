CREATE TABLE IF NOT EXISTS coupons (
    coupon_id        BIGINT PRIMARY KEY,
    coupon_name      VARCHAR(255) NOT NULL,
    coupon_type      VARCHAR(50)  NOT NULL,
    discount_type    VARCHAR(50)  NOT NULL,
    discount_value   INTEGER      NOT NULL,
    min_order_amount INTEGER      NOT NULL DEFAULT 0,
    start_at         TIMESTAMP    NOT NULL,
    end_at           TIMESTAMP    NOT NULL,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS coupon_products (
    coupon_id  BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    CONSTRAINT pk_coupon_products
        PRIMARY KEY (coupon_id, product_id),

    CONSTRAINT fk_coupon_products_coupon
        FOREIGN KEY (coupon_id)
        REFERENCES coupons (coupon_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_coupon_products_product_id
    ON coupon_products (product_id);

CREATE INDEX IF NOT EXISTS idx_coupons_active_period
    ON coupons (is_active, start_at, end_at);

INSERT INTO coupons (
    coupon_id,
    coupon_name,
    coupon_type,
    discount_type,
    discount_value,
    min_order_amount,
    start_at,
    end_at,
    is_active,
    created_at,
    updated_at
)
VALUES
    (
        20001,
        '라면 카테고리 1,000원 할인 쿠폰',
        'PRODUCT',
        'AMOUNT',
        1000,
        0,
        now() - interval '7 days',
        now() + interval '30 days',
        TRUE,
        now(),
        now()
    )
ON CONFLICT (coupon_id) DO NOTHING;

INSERT INTO coupon_products (
    coupon_id,
    product_id
)
VALUES
    (20001, 10001),
    (20001, 10002)
ON CONFLICT (coupon_id, product_id) DO NOTHING;

INSERT INTO promotions (
    promotion_id,
    promotion_name,
    promotion_type,
    min_purchase_amount,
    buy_quantity,
    get_quantity,
    gift_item,
    discount_rate,
    start_at,
    end_at,
    status
)
VALUES
    (
        20001,
        '라면 기획 할인 행사',
        'DISCOUNT',
        NULL,
        NULL,
        NULL,
        NULL,
        15.00,
        now() - interval '7 days',
        now() + interval '30 days',
        'ACTIVE'
    )
ON CONFLICT (promotion_id) DO NOTHING;

INSERT INTO promotion_products (
    promotion_product_id,
    promotion_id,
    product_id,
    promotion_price,
    grid_id
)
VALUES
    (
        20001,
        20001,
        10002,
        4900,
        1
    )
ON CONFLICT (promotion_product_id) DO NOTHING;