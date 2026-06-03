-- Rebuild only product-related schema/data after Product entity changes.
-- Leave unchanged domains (coupon/promotion/order/cart...) untouched.

-- 1) FK cleanup: promotion_products references products
TRUNCATE TABLE promotion_products RESTART IDENTITY;

-- 2) drop product-related tables (drop FK before products; TRUNCATE alone keeps the constraint)
DROP TABLE IF EXISTS inventories;

ALTER TABLE promotion_products
    DROP CONSTRAINT IF EXISTS promotion_products_product_id_fkey;

DROP TABLE IF EXISTS products;

-- 3) recreate products with latest schema
CREATE TABLE products (
    product_id               BIGSERIAL PRIMARY KEY,
    category_id              BIGINT         NOT NULL,
    brand_name               VARCHAR(100),
    product_name             VARCHAR(255)   NOT NULL,
    barcode                  VARCHAR(100),
    external_source          VARCHAR(50),
    external_product_id      VARCHAR(100),
    original_price           INTEGER        NOT NULL,
    description              TEXT,
    image_url                VARCHAR(500),
    sales_unit               VARCHAR(100),
    volume                   VARCHAR(100),
    allergy_info             TEXT,
    badge_text               VARCHAR(100),
    category_confidence      NUMERIC(5, 2),
    category_classified_by   VARCHAR(30),
    category_review_required BOOLEAN        NOT NULL,
    sale_status              VARCHAR(30)    NOT NULL,
    grid_id                  BIGINT,
    deleted_at               TIMESTAMP,
    created_at               TIMESTAMP      NOT NULL,
    updated_at               TIMESTAMP      NOT NULL,
    CONSTRAINT uk_products_barcode UNIQUE (barcode)
);

-- 4) recreate inventories with products FK
CREATE TABLE inventories (
    inventory_id   BIGSERIAL PRIMARY KEY,
    product_id     BIGINT       NOT NULL REFERENCES products (product_id),
    store_id       BIGINT       NOT NULL,
    stock_quantity INTEGER      NOT NULL,
    unit           VARCHAR(30)  NOT NULL,
    stock_status   VARCHAR(30)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

CREATE INDEX idx_inventories_product_id ON inventories (product_id);
CREATE INDEX idx_inventories_store_id ON inventories (store_id);
CREATE INDEX idx_products_grid_id ON products (grid_id);

ALTER TABLE promotion_products
    ADD CONSTRAINT promotion_products_product_id_fkey
        FOREIGN KEY (product_id) REFERENCES products (product_id);

-- 5) reseed products only (inventories stays empty)
INSERT INTO products (
    product_id,
    category_id,
    brand_name,
    product_name,
    barcode,
    external_source,
    external_product_id,
    original_price,
    description,
    image_url,
    sales_unit,
    volume,
    allergy_info,
    badge_text,
    category_confidence,
    category_classified_by,
    category_review_required,
    sale_status,
    grid_id,
    deleted_at,
    created_at,
    updated_at
)
VALUES
    (
        10001,
        1,
        '농심',
        '시드_신라면',
        '8801043014809',
        'SEED',
        'SEED_PRODUCT_10001',
        5000,
        '장바구니, 주문, 프로모션 테스트용 기본 상품입니다.',
        NULL,
        'EA',
        '120g',
        '밀, 대두 함유',
        '테스트상품',
        NULL,
        NULL,
        FALSE,
        'ON_SALE',
        5,
        NULL,
        now(),
        now()
    ),
    (
        10002,
        1,
        '농심',
        '시드_사리곰탕면',
        '8801043015943',
        'SEED',
        'SEED_PRODUCT_10002',
        6000,
        '재고 부족 및 대체 상품 추천 테스트용 기본 상품입니다.',
        NULL,
        'EA',
        '110g',
        '밀, 대두, 우유 함유',
        '품절임박',
        NULL,
        NULL,
        FALSE,
        'ON_SALE',
        8,
        NULL,
        now(),
        now()
    ),
    (
        10003,
        3,
        '햇반',
        '시드_백미밥 210g',
        '8801007310572',
        'SEED',
        'SEED_PRODUCT_10003',
        24000,
        '연관 상품 추천 및 쿠폰 적용 테스트용 기본 상품입니다.',
        NULL,
        'BOX',
        '210g x 12개',
        NULL,
        '추천상품',
        NULL,
        NULL,
        FALSE,
        'ON_SALE',
        129,
        NULL,
        now(),
        now()
    );

SELECT setval(
               pg_get_serial_sequence('products', 'product_id'),
               COALESCE((SELECT MAX(product_id) FROM products), 1)
       );
