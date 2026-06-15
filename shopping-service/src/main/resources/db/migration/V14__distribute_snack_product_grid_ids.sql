-- category_id=20(과자) 상품 grid_id를 CategoryGridIdResolver 후보에 균등 분배한다.
-- V13 HACCP 시드는 categories.grid_id 기본값(120)만 사용해 전부 120으로 들어갔다.

WITH snack_grid_candidates AS (
    SELECT grid_id, ordinality - 1 AS idx
    FROM unnest(ARRAY[
        61::BIGINT, 90, 119, 148, 62, 91, 120, 149, 64, 93, 122, 151
    ]) WITH ORDINALITY AS t(grid_id, ordinality)
),
ranked_snacks AS (
    SELECT
        product_id,
        (ROW_NUMBER() OVER (ORDER BY product_id) - 1) % 12 AS idx
    FROM products
    WHERE category_id = 20
)
UPDATE products p
SET grid_id = c.grid_id,
    updated_at = now()
FROM ranked_snacks rs
JOIN snack_grid_candidates c ON c.idx = rs.idx
WHERE p.product_id = rs.product_id;
