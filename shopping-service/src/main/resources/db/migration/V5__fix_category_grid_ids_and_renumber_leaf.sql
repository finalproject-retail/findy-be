-- V5: V4 category grid corrections (V4 values stay immutable).
-- Prerequisite: V4 (categories).
-- Remove duplicate leaf category 42, renumber leaf 43-46 -> 42-45, then fix tail grid_ids.

-- 1) Leaf grid_id corrections (pre-renumber category_id)
UPDATE categories SET grid_id = 253, updated_at = now() WHERE category_id = 12;
UPDATE categories SET grid_id = 398, updated_at = now() WHERE category_id = 13;
UPDATE categories SET grid_id = 140, updated_at = now() WHERE category_id = 14;
UPDATE categories SET grid_id = 103, updated_at = now() WHERE category_id = 26;
UPDATE categories SET grid_id = 367, updated_at = now() WHERE category_id = 29;
UPDATE categories SET grid_id = 251, updated_at = now() WHERE category_id = 30;
UPDATE categories SET grid_id = 425, updated_at = now() WHERE category_id = 31;
UPDATE categories SET grid_id = 355, updated_at = now() WHERE category_id = 35;
UPDATE categories SET grid_id = 415, updated_at = now() WHERE category_id = 36;
UPDATE categories SET grid_id = 413, updated_at = now() WHERE category_id = 37;

-- 2) 중복 leaf 42에 붙은 상품 삭제 (잘못된 카테고리 데이터)
DELETE FROM promotion_products
WHERE product_id IN (SELECT product_id FROM products WHERE category_id = 42);

DELETE FROM order_items
WHERE product_id IN (SELECT product_id FROM products WHERE category_id = 42);

DELETE FROM shopping_list_items
WHERE product_id IN (SELECT product_id FROM products WHERE category_id = 42);

DELETE FROM cart_items
WHERE product_id IN (SELECT product_id FROM products WHERE category_id = 42);

DELETE FROM inventories
WHERE product_id IN (SELECT product_id FROM products WHERE category_id = 42);

DELETE FROM products WHERE category_id = 42;

-- 3) Remove duplicate leaf 42 (디지털 기기 under 의류/214)
DELETE FROM categories
WHERE category_id = 42
  AND category_name = '디지털 기기'
  AND parent_category_id = 214;

-- 4) Renumber leaf 43-46 -> 42-45
INSERT INTO categories (category_id, parent_category_id, category_name, is_active, grid_id, created_at, updated_at)
SELECT category_id + 9000, parent_category_id, category_name, is_active, grid_id, now(), now()
FROM categories
WHERE category_id IN (43, 44, 45, 46);

DELETE FROM categories WHERE category_id IN (43, 44, 45, 46);

INSERT INTO categories (category_id, parent_category_id, category_name, is_active, grid_id, created_at, updated_at)
SELECT 42, parent_category_id, category_name, is_active, grid_id, now(), now()
FROM categories WHERE category_id = 9043;

INSERT INTO categories (category_id, parent_category_id, category_name, is_active, grid_id, created_at, updated_at)
SELECT 43, parent_category_id, category_name, is_active, grid_id, now(), now()
FROM categories WHERE category_id = 9044;

INSERT INTO categories (category_id, parent_category_id, category_name, is_active, grid_id, created_at, updated_at)
SELECT 44, parent_category_id, category_name, is_active, grid_id, now(), now()
FROM categories WHERE category_id = 9045;

INSERT INTO categories (category_id, parent_category_id, category_name, is_active, grid_id, created_at, updated_at)
SELECT 45, parent_category_id, category_name, is_active, grid_id, now(), now()
FROM categories WHERE category_id = 9046;

DELETE FROM categories WHERE category_id IN (9043, 9044, 9045, 9046);

-- 5) Tail grid_id after renumber (42=신발/가방, 43=가구/침구)
UPDATE categories SET grid_id = 235, updated_at = now() WHERE category_id = 42;
UPDATE categories SET grid_id = 210, updated_at = now() WHERE category_id = 43;

-- 6) Align product.category_id with renumbered leaves
UPDATE products
SET category_id = category_id - 1,
    updated_at = now()
WHERE category_id BETWEEN 43 AND 46;
