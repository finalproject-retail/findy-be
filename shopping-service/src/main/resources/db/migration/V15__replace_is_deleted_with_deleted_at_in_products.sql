-- Replace boolean soft-delete flag with timestamp soft-delete column.
-- is_deleted=false -> deleted_at=NULL
-- is_deleted=true  -> deleted_at=updated_at/created_at/NOW()

DO $$
BEGIN
	-- Case 1: old schema only has is_deleted.
	-- Rename it first, then convert boolean to timestamp.
	IF EXISTS (
		SELECT 1
		FROM information_schema.columns
		WHERE table_schema = CURRENT_SCHEMA()
		  AND table_name = 'products'
		  AND column_name = 'is_deleted'
	) AND NOT EXISTS (
		SELECT 1
		FROM information_schema.columns
		WHERE table_schema = CURRENT_SCHEMA()
		  AND table_name = 'products'
		  AND column_name = 'deleted_at'
	) THEN
ALTER TABLE products RENAME COLUMN is_deleted TO deleted_at;

ALTER TABLE products
    ALTER COLUMN deleted_at DROP DEFAULT;

ALTER TABLE products
ALTER COLUMN deleted_at TYPE TIMESTAMP
		USING CASE
			WHEN deleted_at = TRUE THEN COALESCE(updated_at, created_at, NOW())
			ELSE NULL
END;

ALTER TABLE products
    ALTER COLUMN deleted_at DROP NOT NULL;
END IF;

	-- Case 2: both columns exist.
	-- Copy old true values into deleted_at, then drop is_deleted.
	IF EXISTS (
		SELECT 1
		FROM information_schema.columns
		WHERE table_schema = CURRENT_SCHEMA()
		  AND table_name = 'products'
		  AND column_name = 'is_deleted'
	) AND EXISTS (
		SELECT 1
		FROM information_schema.columns
		WHERE table_schema = CURRENT_SCHEMA()
		  AND table_name = 'products'
		  AND column_name = 'deleted_at'
	) THEN
UPDATE products
SET deleted_at = COALESCE(updated_at, created_at, NOW())
WHERE deleted_at IS NULL
  AND is_deleted = TRUE;

ALTER TABLE products DROP COLUMN is_deleted;
END IF;

	-- Case 3: neither column exists.
	-- Add deleted_at for safety.
	IF NOT EXISTS (
		SELECT 1
		FROM information_schema.columns
		WHERE table_schema = CURRENT_SCHEMA()
		  AND table_name = 'products'
		  AND column_name = 'deleted_at'
	) THEN
ALTER TABLE products
    ADD COLUMN deleted_at TIMESTAMP NULL;
END IF;
END $$;