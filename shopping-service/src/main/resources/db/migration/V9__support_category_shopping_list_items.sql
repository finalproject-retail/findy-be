ALTER TABLE shopping_list_items
    ADD COLUMN IF NOT EXISTS item_type VARCHAR(30);

UPDATE shopping_list_items
SET item_type = 'PRODUCT'
WHERE item_type IS NULL;

ALTER TABLE shopping_list_items
    ALTER COLUMN item_type SET NOT NULL;

ALTER TABLE shopping_list_items
    ALTER COLUMN product_id DROP NOT NULL;

ALTER TABLE shopping_list_items
    ADD COLUMN IF NOT EXISTS category_id BIGINT;

ALTER TABLE shopping_list_items
    ADD COLUMN IF NOT EXISTS category_name VARCHAR(255);

ALTER TABLE shopping_list_items
    ADD COLUMN IF NOT EXISTS is_checked BOOLEAN;

UPDATE shopping_list_items
SET is_checked = CASE
                     WHEN scanned_quantity >= quantity THEN TRUE
                     ELSE FALSE
    END
WHERE is_checked IS NULL;

ALTER TABLE shopping_list_items
    ALTER COLUMN is_checked SET NOT NULL;

ALTER TABLE shopping_list_items
    ADD CONSTRAINT ck_shopping_list_items_item_type
        CHECK (item_type IN ('PRODUCT', 'CATEGORY'));

ALTER TABLE shopping_list_items
    ADD CONSTRAINT ck_shopping_list_items_target
        CHECK (
            (item_type = 'PRODUCT' AND product_id IS NOT NULL)
                OR
            (item_type = 'CATEGORY' AND product_id IS NULL AND category_name IS NOT NULL)
            );

ALTER TABLE shopping_list_items
    ADD CONSTRAINT fk_shopping_list_items_category_id
        FOREIGN KEY (category_id) REFERENCES categories (category_id);

CREATE INDEX IF NOT EXISTS idx_shopping_list_items_item_type
    ON shopping_list_items (item_type);

CREATE INDEX IF NOT EXISTS idx_shopping_list_items_category_id
    ON shopping_list_items (category_id);