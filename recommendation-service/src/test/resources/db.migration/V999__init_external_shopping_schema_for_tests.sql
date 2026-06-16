-- Test-only external schema required by recommendation-service JPA validation.
-- recommendation-service references shopping_service.products,
-- but CI test DB only runs recommendation-service migrations.

CREATE SCHEMA IF NOT EXISTS shopping_service;

CREATE TABLE IF NOT EXISTS shopping_service.products
(
    product_id               BIGINT PRIMARY KEY,
    category_id              BIGINT,
    brand_name               VARCHAR(100),
    product_name             VARCHAR(255),
    barcode                  VARCHAR(100),
    external_source          VARCHAR(50),
    external_product_id      VARCHAR(100),
    original_price           INTEGER,
    sale_price               INTEGER,
    discount_rate            NUMERIC(5, 2),
    description              TEXT,
    image_url                VARCHAR(500),
    packaging_type           VARCHAR(100),
    sales_unit               VARCHAR(100),
    volume                   VARCHAR(100),
    allergy_info             TEXT,
    badge_text               VARCHAR(100),
    category_confidence      NUMERIC(5, 2),
    category_classified_by   VARCHAR(30),
    category_review_required BOOLEAN DEFAULT FALSE,
    sale_status              VARCHAR(30),
    grid_id                  BIGINT,
    deleted_at               TIMESTAMP NULL,
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP
);

ALTER TABLE shopping_service.products
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;