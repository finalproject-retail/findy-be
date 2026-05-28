-- Add category master table and product/category grid mapping.
-- Leaf category IDs (1..46) are kept to match ProductCategoryCatalog.

CREATE TABLE IF NOT EXISTS categories (
    category_id         BIGINT PRIMARY KEY,
    parent_category_id  BIGINT REFERENCES categories (category_id),
    category_name       VARCHAR(255) NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    grid_id             BIGINT,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS grid_id BIGINT;

INSERT INTO categories (category_id, parent_category_id, category_name, is_active, grid_id)
VALUES
    -- depth 1
    (101, NULL, '신선 식품', TRUE, NULL),
    (102, NULL, '가공/냉동 식품', TRUE, NULL),
    (103, NULL, '베이커리/델리', TRUE, NULL),
    (104, NULL, '음료/주류', TRUE, NULL),
    (105, NULL, '라이프 스타일', TRUE, NULL),

    -- depth 2
    (201, 101, '농산', TRUE, NULL),
    (202, 101, '축산', TRUE, NULL),
    (203, 101, '수산', TRUE, NULL),
    (204, 101, '유제품/냉장', TRUE, NULL),
    (205, 102, '냉동', TRUE, NULL),
    (206, 102, '면/통조림', TRUE, NULL),
    (207, 102, '스낵/캔디', TRUE, NULL),
    (208, 103, '베이커리', TRUE, NULL),
    (209, 103, '델리', TRUE, NULL),
    (210, 104, '음료', TRUE, NULL),
    (211, 104, '주류', TRUE, NULL),
    (212, 105, '주방/생활', TRUE, NULL),
    (213, 105, '가전/IT', TRUE, NULL),
    (214, 105, '의류/잡화', TRUE, NULL),
    (215, 105, '홈케어/캠핑', TRUE, NULL),

    -- depth 3 (leaf; grid_id = gridY * 29 + gridX + 1, aligned with findy-fe emart-floor-plan)
    (1, 201, '과일', TRUE, 521),          -- 하단 농산 (27,17)
    (2, 201, '채소/샐러드', TRUE, 435),      -- 우측 농산 (28,14)
    (3, 201, '견과류', TRUE, 117),          -- 좌측 견과 (0,4)
    (4, 202, '소고기', TRUE, 28),           -- 상단 냉장/축산 (27,0)
    (5, 202, '돼지고기', TRUE, 87),          -- 우측 냉장/축산 (28,2)
    (6, 202, '닭고기', TRUE, 116),          -- 우측 냉장/축산 (28,3)
    (7, 203, '회/초밥', TRUE, 261),         -- 우측 수산 (28,8)
    (8, 203, '수산물', TRUE, 290),          -- 우측 수산 (28,9)
    (9, 203, '건어물', TRUE, 348),          -- 우측 수산 (28,11)
    (10, 204, '우유/요거트', TRUE, 137),      -- 상단섬 유제품 (20,4)
    (11, 204, '치즈/버터', TRUE, 135),       -- 상단섬 유제품 (18,4)
    (12, 204, '햄/소시지', TRUE, 136),       -- 상단섬 유제품 (19,4)
    (13, 204, '밀키트', TRUE, 22),          -- 상단 델리 (21,0)
    (14, 205, '만두/피자', TRUE, 141),       -- 우측 냉동 (24,4)
    (15, 205, '간편식', TRUE, 286),         -- 우측 냉동 (24,9)
    (16, 205, '냉동 과일/디저트', TRUE, 431),   -- 우측 냉동 (24,14)
    (17, 206, '라면', TRUE, 6),            -- 상단 라면 (5,0)
    (18, 206, '즉석밥', TRUE, 129),         -- 상단섬 즉석밥 (12,4)
    (19, 206, '통조림', TRUE, 131),         -- 상단섬 통조림 (14,4)
    (20, 207, '과자', TRUE, 120),          -- 상단섬 과자 (3,4)
    (21, 207, '초콜릿/젤리', TRUE, 123),      -- 상단섬 초콜릿 (6,4)
    (22, 207, '시리얼', TRUE, 126),         -- 상단섬 시리얼 (9,4)
    (23, 208, '빵/베이글', TRUE, 14),        -- 상단 베이커리 (13,0)
    (24, 208, '케이크', TRUE, 15),          -- 상단 베이커리 (14,0)
    (25, 208, '쿠키', TRUE, 16),           -- 상단 베이커리 (15,0)
    (26, 209, '치킨', TRUE, 133),          -- 상단섬 델리 (16,4)
    (27, 209, '꼬치류', TRUE, 134),         -- 상단섬 델리 (17,4)
    (28, 209, '일품요리', TRUE, 132),       -- 상단섬 델리 (15,4)
    (29, 210, '생수/탄산수', TRUE, 424),      -- 하단섬 음료 (18,14)
    (30, 210, '탄산음료', TRUE, 278),       -- 중단섬 음료 (17,9)
    (31, 210, '커피/차', TRUE, 279),        -- 중단섬 음료 (18,9)
    (32, 211, '와인/양주', TRUE, 515),       -- 하단 주류 (21,17)
    (33, 211, '맥주', TRUE, 513),          -- 하단 주류 (19,17)
    (34, 211, '전통주', TRUE, 517),         -- 하단 주류 (23,17)
    (35, 212, '세제/섬유유연제', TRUE, 267),   -- 중단섬 홈케어 (6,9)
    (36, 212, '일회용품', TRUE, 412),       -- 하단섬 주방/생활 (6,14)
    (37, 212, '주방용품', TRUE, 413),       -- 하단섬 주방/생활 (7,14)
    (38, 213, '대형 가전', TRUE, 507),       -- 하단 가전 (13,17)
    (39, 213, '디지털 기기', TRUE, 418),     -- 하단섬 가전 (12,14)
    (40, 213, '소형 전자제품', TRUE, 421),    -- 하단섬 가전 (15,14)
    (41, 214, '의류', TRUE, 409),          -- 하단섬 의류 (3,14)
    (42, 214, '디지털 기기', TRUE, 264),     -- 중단섬 잡화 (3,9)
    (43, 214, '신발/가방', TRUE, 410),       -- 하단섬 의류 (4,14)
    (44, 215, '가구/침구', TRUE, 273),       -- 중단섬 캠핑 (12,9)
    (45, 215, '캠핑/아웃도어 용품', TRUE, 276), -- 중단섬 캠핑 (15,9)
    (46, 215, '차량 용품', TRUE, 279)       -- 중단섬 차량용품 (18,9)
ON CONFLICT (category_id) DO UPDATE
SET parent_category_id = EXCLUDED.parent_category_id,
    category_name = EXCLUDED.category_name,
    is_active = EXCLUDED.is_active,
    grid_id = EXCLUDED.grid_id,
    updated_at = now();

-- Default product grid fallback from leaf category grid when product.grid_id is empty.
UPDATE products p
SET grid_id = c.grid_id
FROM categories c
WHERE p.category_id = c.category_id
  AND p.grid_id IS NULL
  AND c.grid_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_categories_parent_category_id
    ON categories (parent_category_id);

CREATE INDEX IF NOT EXISTS idx_categories_grid_id
    ON categories (grid_id);

CREATE INDEX IF NOT EXISTS idx_products_grid_id
    ON products (grid_id);
