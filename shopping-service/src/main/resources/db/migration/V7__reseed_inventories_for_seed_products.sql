-- V6 reseeded products but left inventories empty.
-- Drop stock_status (derived from stock_quantity in app) and restore seed stock rows.

ALTER TABLE inventories
    DROP COLUMN IF EXISTS stock_status;

INSERT INTO inventories (product_id, store_id, stock_quantity, unit, created_at, updated_at)
SELECT 10001, 1, 100, 'EA', now(), now()
WHERE EXISTS (SELECT 1 FROM products WHERE product_id = 10001)
  AND NOT EXISTS (
    SELECT 1 FROM inventories WHERE product_id = 10001 AND store_id = 1
  );

INSERT INTO inventories (product_id, store_id, stock_quantity, unit, created_at, updated_at)
SELECT 10002, 1, 3, 'EA', now(), now()
WHERE EXISTS (SELECT 1 FROM products WHERE product_id = 10002)
  AND NOT EXISTS (
    SELECT 1 FROM inventories WHERE product_id = 10002 AND store_id = 1
  );

INSERT INTO inventories (product_id, store_id, stock_quantity, unit, created_at, updated_at)
SELECT 10003, 1, 0, 'EA', now(), now()
WHERE EXISTS (SELECT 1 FROM products WHERE product_id = 10003)
  AND NOT EXISTS (
    SELECT 1 FROM inventories WHERE product_id = 10003 AND store_id = 1
  );
