-- recommendation-service snapshot seed data

INSERT INTO categories (
    category_id,
    parent_category_id,
    category_name,
    is_active
)
VALUES
    (1, NULL, '라면/면류', TRUE),
    (3, NULL, '즉석밥/간편식', TRUE),
    (5, NULL, '유제품', TRUE)
    ON CONFLICT (category_id) DO NOTHING;

INSERT INTO shopping_styles (
    shopping_style_id,
    style_name,
    is_active
)
VALUES
    (1, '1인 가구', TRUE),
    (2, '신선도 중시', TRUE),
    (5, '가성비', TRUE),
    (6, '빠른 쇼핑', TRUE),
    (8, '건강/유기농', TRUE)
    ON CONFLICT (shopping_style_id) DO NOTHING;

INSERT INTO products (
    product_id,
    category_id,
    brand_name,
    product_name,
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
        5000,
        4500,
        10.00,
        '개인 맞춤 추천 테스트용 라면 상품입니다.',
        NULL,
        '봉지',
        'EA',
        '120g',
        '밀, 대두 함유',
        '테스트상품',
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
        6000,
        5400,
        10.00,
        '재고 부족 대체 추천 테스트용 라면 상품입니다.',
        NULL,
        '봉지',
        'EA',
        '110g',
        '밀, 대두, 우유 함유',
        '품절임박',
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
        24000,
        21000,
        12.50,
        '연관 상품 추천 테스트용 즉석밥 상품입니다.',
        NULL,
        '상온',
        'BOX',
        '210g x 12개',
        NULL,
        '추천상품',
        FALSE,
        'ON_SALE',
        FALSE,
        now(),
        now()
    ),
    (
        10004,
        5,
        '서울우유',
        '시드_우유 1L',
        3200,
        2900,
        9.38,
        '개인 맞춤 추천 테스트용 유제품입니다.',
        NULL,
        '냉장',
        'EA',
        '1L',
        '우유 함유',
        '신선식품',
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
    (10001, 10001, 1, 100, 'EA', 'IN_STOCK', now(), now()),
    (10002, 10002, 1, 3, 'EA', 'LOW_STOCK', now(), now()),
    (10003, 10003, 1, 50, 'EA', 'IN_STOCK', now(), now()),
    (10004, 10004, 1, 30, 'EA', 'IN_STOCK', now(), now())
    ON CONFLICT (inventory_id) DO NOTHING;

INSERT INTO user_preferred_categories (
    user_preferred_category_id,
    user_id,
    category_id
)
VALUES
    (10001, 1, 1),
    (10002, 1, 3),
    (10003, 1, 5)
    ON CONFLICT (user_preferred_category_id) DO NOTHING;

INSERT INTO user_shopping_styles (
    user_shopping_style_id,
    user_id,
    shopping_style_id
)
VALUES
    (10001, 1, 2),
    (10002, 1, 5),
    (10003, 1, 6)
    ON CONFLICT (user_shopping_style_id) DO NOTHING;

INSERT INTO product_embeddings (
    product_embedding_id,
    product_id,
    model,
    dimensions,
    embedding,
    created_at,
    updated_at
)
VALUES
    (
        10001,
        10001,
        'seed-embedding',
        5,
        '0.10,0.20,0.30,0.40,0.50',
        now(),
        now()
    ),
    (
        10002,
        10002,
        'seed-embedding',
        5,
        '0.11,0.21,0.31,0.41,0.51',
        now(),
        now()
    ),
    (
        10003,
        10003,
        'seed-embedding',
        5,
        '0.50,0.40,0.30,0.20,0.10',
        now(),
        now()
    ),
    (
        10004,
        10004,
        'seed-embedding',
        5,
        '0.20,0.10,0.40,0.30,0.60',
        now(),
        now()
    )
    ON CONFLICT (product_id) DO NOTHING;

SELECT setval(
               pg_get_serial_sequence('product_embeddings', 'product_embedding_id'),
               COALESCE((SELECT MAX(product_embedding_id) FROM product_embeddings), 1)
       );