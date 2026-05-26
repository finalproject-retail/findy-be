-- shopping-service base seed data

INSERT INTO products (
    product_id,
    category_id,
    brand_name,
    product_name,
    barcode,
    external_source,
    external_product_id,
    original_price,
    sale_price,
    discount_rate,
    description,
    image_url,
    packaging_type,
    sales_unit,
    volume,
    allergy_info,
    badge_text,
    category_confidence,
    category_classified_by,
    category_review_required,
    sale_status,
    is_deleted,
    created_at,
    updated_at
)
VALUES
    (
        10001,
        1,
        '농심',
        '시드_신라면',
        '8800000000001',
        'SEED',
        'SEED_PRODUCT_10001',
        5000,
        4500,
        10.00,
        '장바구니, 주문, 프로모션 테스트용 기본 상품입니다.',
        NULL,
        '봉지',
        'EA',
        '120g',
        '밀, 대두 함유',
        '테스트상품',
        NULL,
        NULL,
        FALSE,
        'ON_SALE',
        FALSE,
        now(),
        now()
    ),
    (
        10002,
        1,
        '농심',
        '시드_사리곰탕면',
        '8800000000002',
        'SEED',
        'SEED_PRODUCT_10002',
        6000,
        5400,
        10.00,
        '재고 부족 및 대체 상품 추천 테스트용 기본 상품입니다.',
        NULL,
        '봉지',
        'EA',
        '110g',
        '밀, 대두, 우유 함유',
        '품절임박',
        NULL,
        NULL,
        FALSE,
        'ON_SALE',
        FALSE,
        now(),
        now()
    ),
    (
        10003,
        3,
        '햇반',
        '시드_백미밥 210g',
        '8800000000003',
        'SEED',
        'SEED_PRODUCT_10003',
        24000,
        21000,
        12.50,
        '연관 상품 추천 및 쿠폰 적용 테스트용 기본 상품입니다.',
        NULL,
        '상온',
        'BOX',
        '210g x 12개',
        NULL,
        '추천상품',
        NULL,
        NULL,
        FALSE,
        'ON_SALE',
        FALSE,
        now(),
        now()
    )
    ON CONFLICT (product_id) DO NOTHING;

INSERT INTO inventories (
    inventory_id,
    product_id,
    store_id,
    stock_quantity,
    unit,
    stock_status,
    created_at,
    updated_at
)
VALUES
    (
        10001,
        10001,
        1,
        100,
        'EA',
        'IN_STOCK',
        now(),
        now()
    ),
    (
        10002,
        10002,
        1,
        3,
        'EA',
        'LOW_STOCK',
        now(),
        now()
    ),
    (
        10003,
        10003,
        1,
        50,
        'EA',
        'IN_STOCK',
        now(),
        now()
    )
    ON CONFLICT (inventory_id) DO NOTHING;

INSERT INTO coupons (
    coupon_id,
    coupon_name,
    coupon_type,
    discount_type,
    discount_value,
    is_stackable,
    min_order_amount,
    start_at,
    end_at,
    period_type,
    days_limit,
    is_active,
    created_at,
    updated_at
)
VALUES
    (
        10001,
        '시드_전체 상품 1000원 할인 쿠폰',
        'ALL',
        'AMOUNT',
        1000,
        FALSE,
        10000,
        TIMESTAMP '2026-01-01 00:00:00',
        TIMESTAMP '2026-12-31 23:59:59',
        'ABSOLUTE',
        NULL,
        TRUE,
        now(),
        now()
    ),
    (
        10002,
        '시드_라면 카테고리 10% 할인 쿠폰',
        'PRODUCT',
        'RATE',
        10,
        FALSE,
        0,
        TIMESTAMP '2026-01-01 00:00:00',
        TIMESTAMP '2026-12-31 23:59:59',
        'ABSOLUTE',
        NULL,
        TRUE,
        now(),
        now()
    )
    ON CONFLICT (coupon_id) DO NOTHING;

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
    status,
    created_at,
    updated_at
)
VALUES
    (
        10001,
        '시드_라면 10% 할인 행사',
        'DISCOUNT',
        NULL,
        NULL,
        NULL,
        NULL,
        10.00,
        TIMESTAMP '2026-01-01 00:00:00',
        TIMESTAMP '2026-12-31 23:59:59',
        'ACTIVE',
        now(),
        now()
    ),
    (
        10002,
        '시드_음료 2+2 행사',
        'BOGO',
        NULL,
        2,
        2,
        NULL,
        NULL,
        TIMESTAMP '2026-01-01 00:00:00',
        TIMESTAMP '2026-12-31 23:59:59',
        'ACTIVE',
        now(),
        now()
    )
    ON CONFLICT (promotion_id) DO NOTHING;

INSERT INTO promotion_products (
    promotion_product_id,
    promotion_id,
    product_id,
    promotion_price,
    grid_id,
    created_at,
    updated_at
)
VALUES
    (
        10001,
        10001,
        10001,
        4000,
        1,
        now(),
        now()
    ),
    (
        10002,
        10002,
        10002,
        NULL,
        1,
        now(),
        now()
    )
    ON CONFLICT (promotion_product_id) DO NOTHING;

SELECT setval(
               pg_get_serial_sequence('products', 'product_id'),
               COALESCE((SELECT MAX(product_id) FROM products), 1)
       );

SELECT setval(
               pg_get_serial_sequence('inventories', 'inventory_id'),
               COALESCE((SELECT MAX(inventory_id) FROM inventories), 1)
       );

SELECT setval(
               pg_get_serial_sequence('coupons', 'coupon_id'),
               COALESCE((SELECT MAX(coupon_id) FROM coupons), 1)
       );

SELECT setval(
               pg_get_serial_sequence('promotions', 'promotion_id'),
               COALESCE((SELECT MAX(promotion_id) FROM promotions), 1)
       );

SELECT setval(
               pg_get_serial_sequence('promotion_products', 'promotion_product_id'),
               COALESCE((SELECT MAX(promotion_product_id) FROM promotion_products), 1)
       );