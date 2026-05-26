-- user-service preference seed data

INSERT INTO shopping_styles (
    shopping_style_id,
    style_name,
    is_active
)
VALUES
    (1, '1인 가구', TRUE),
    (2, '신선도 중시', TRUE),
    (3, '비건', TRUE),
    (4, '다가구', TRUE),
    (5, '가성비', TRUE),
    (6, '빠른 쇼핑', TRUE),
    (7, '단체 행사', TRUE),
    (8, '건강/유기농', TRUE)
    ON CONFLICT (shopping_style_id) DO NOTHING;

INSERT INTO user_preferred_categories (
    user_preferred_category_id,
    user_id,
    category_id
)
SELECT 1, 1, 1
    WHERE NOT EXISTS (
    SELECT 1 FROM user_preferred_categories WHERE user_preferred_category_id = 1
);

INSERT INTO user_preferred_categories (
    user_preferred_category_id,
    user_id,
    category_id
)
SELECT 2, 1, 3
    WHERE NOT EXISTS (
    SELECT 1 FROM user_preferred_categories WHERE user_preferred_category_id = 2
);

INSERT INTO user_preferred_categories (
    user_preferred_category_id,
    user_id,
    category_id
)
SELECT 3, 1, 5
    WHERE NOT EXISTS (
    SELECT 1 FROM user_preferred_categories WHERE user_preferred_category_id = 3
);

INSERT INTO user_shopping_styles (
    user_shopping_style_id,
    user_id,
    shopping_style_id
)
SELECT 1, 1, 2
    WHERE NOT EXISTS (
    SELECT 1 FROM user_shopping_styles WHERE user_shopping_style_id = 1
);

INSERT INTO user_shopping_styles (
    user_shopping_style_id,
    user_id,
    shopping_style_id
)
SELECT 2, 1, 5
    WHERE NOT EXISTS (
    SELECT 1 FROM user_shopping_styles WHERE user_shopping_style_id = 2
);

INSERT INTO user_shopping_styles (
    user_shopping_style_id,
    user_id,
    shopping_style_id
)
SELECT 3, 1, 6
    WHERE NOT EXISTS (
    SELECT 1 FROM user_shopping_styles WHERE user_shopping_style_id = 3
);

SELECT setval(
               pg_get_serial_sequence('shopping_styles', 'shopping_style_id'),
               COALESCE((SELECT MAX(shopping_style_id) FROM shopping_styles), 1)
       );

SELECT setval(
               pg_get_serial_sequence('user_preferred_categories', 'user_preferred_category_id'),
               COALESCE((SELECT MAX(user_preferred_category_id) FROM user_preferred_categories), 1)
       );

SELECT setval(
               pg_get_serial_sequence('user_shopping_styles', 'user_shopping_style_id'),
               COALESCE((SELECT MAX(user_shopping_style_id) FROM user_shopping_styles), 1)
       );