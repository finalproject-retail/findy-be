-- shopping-service initial schema (matches JPA entities)

CREATE TABLE IF NOT EXISTS products (
    product_id               BIGSERIAL PRIMARY KEY,
    category_id              BIGINT         NOT NULL,
    brand_name               VARCHAR(100),
    product_name             VARCHAR(255)   NOT NULL,
    barcode                  VARCHAR(100),
    external_source          VARCHAR(50),
    external_product_id      VARCHAR(100),
    original_price           INTEGER        NOT NULL,
    sale_price               INTEGER        NOT NULL,
    discount_rate            NUMERIC(5, 2)  NOT NULL,
    description              TEXT,
    image_url                VARCHAR(500),
    packaging_type           VARCHAR(100),
    sales_unit               VARCHAR(100),
    volume                   VARCHAR(100),
    allergy_info             TEXT,
    badge_text               VARCHAR(100),
    category_confidence      NUMERIC(5, 2),
    category_classified_by   VARCHAR(30),
    category_review_required BOOLEAN        NOT NULL,
    sale_status              VARCHAR(30)    NOT NULL,
    is_deleted               BOOLEAN        NOT NULL,
    created_at               TIMESTAMP      NOT NULL,
    updated_at               TIMESTAMP      NOT NULL,
    CONSTRAINT uk_products_barcode UNIQUE (barcode)
);

CREATE TABLE IF NOT EXISTS coupons (
    coupon_id        BIGSERIAL PRIMARY KEY,
    coupon_name      VARCHAR(255) NOT NULL,
    coupon_type      VARCHAR(255) NOT NULL,
    discount_type    VARCHAR(255) NOT NULL,
    discount_value   INTEGER      NOT NULL,
    is_stackable     BOOLEAN      NOT NULL,
    min_order_amount INTEGER      NOT NULL,
    start_at         TIMESTAMP    NOT NULL,
    end_at           TIMESTAMP    NOT NULL,
    period_type      VARCHAR(255) NOT NULL,
    days_limit       INTEGER,
    is_active        BOOLEAN      NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS inventories (
    inventory_id   BIGSERIAL PRIMARY KEY,
    product_id     BIGINT       NOT NULL REFERENCES products (product_id),
    store_id       BIGINT       NOT NULL,
    stock_quantity INTEGER      NOT NULL,
    unit           VARCHAR(30)  NOT NULL,
    stock_status   VARCHAR(30)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS carts (
    cart_id    BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_id BIGSERIAL PRIMARY KEY,
    cart_id      BIGINT    NOT NULL REFERENCES carts (cart_id),
    product_id   BIGINT    NOT NULL,
    quantity     INTEGER   NOT NULL,
    is_checked   BOOLEAN   NOT NULL,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS shopping_lists (
    shopping_list_id BIGSERIAL PRIMARY KEY,
    user_id          BIGINT    NOT NULL,
    cart_id          BIGINT    NOT NULL UNIQUE REFERENCES carts (cart_id),
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS shopping_list_items (
    shopping_list_item_id BIGSERIAL PRIMARY KEY,
    shopping_list_id      BIGINT    NOT NULL REFERENCES shopping_lists (shopping_list_id),
    product_id            BIGINT    NOT NULL,
    quantity              INTEGER   NOT NULL,
    scanned_quantity      INTEGER   NOT NULL,
    scanned_at            TIMESTAMP,
    created_at            TIMESTAMP NOT NULL,
    updated_at            TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS user_coupons (
    user_coupon_id BIGSERIAL PRIMARY KEY,
    user_id        BIGINT    NOT NULL,
    coupon_id      BIGINT    NOT NULL REFERENCES coupons (coupon_id),
    is_used        BOOLEAN   NOT NULL,
    downloaded_at  TIMESTAMP NOT NULL,
    expires_at     TIMESTAMP NOT NULL,
    used_at        TIMESTAMP,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    order_id         BIGSERIAL PRIMARY KEY,
    user_id          BIGINT    NOT NULL,
    shopping_list_id BIGINT,
    coupon_id        BIGINT,
    total_amount     INTEGER   NOT NULL,
    discount_amount  INTEGER   NOT NULL,
    final_amount     INTEGER   NOT NULL,
    earned_reward    INTEGER   NOT NULL,
    order_status     VARCHAR(30) NOT NULL,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id   BIGSERIAL PRIMARY KEY,
    order_id        BIGINT    NOT NULL REFERENCES orders (order_id),
    cart_item_id    BIGINT,
    product_id      BIGINT    NOT NULL,
    quantity        INTEGER   NOT NULL,
    product_price   INTEGER   NOT NULL,
    discount_amount INTEGER   NOT NULL,
    final_amount    INTEGER   NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_inventories_product_id ON inventories (product_id);
CREATE INDEX IF NOT EXISTS idx_inventories_store_id ON inventories (store_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX IF NOT EXISTS idx_shopping_list_items_shopping_list_id ON shopping_list_items (shopping_list_id);
CREATE INDEX IF NOT EXISTS idx_user_coupons_user_id ON user_coupons (user_id);
CREATE INDEX IF NOT EXISTS idx_user_coupons_coupon_id ON user_coupons (coupon_id);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders (user_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items (order_id);
