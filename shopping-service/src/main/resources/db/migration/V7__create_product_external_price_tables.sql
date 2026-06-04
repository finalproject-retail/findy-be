CREATE TABLE IF NOT EXISTS product_external_mappings (
    product_external_mapping_id BIGSERIAL PRIMARY KEY,
    product_id                  BIGINT        NOT NULL REFERENCES products (product_id),
    external_source             VARCHAR(30)   NOT NULL,
    external_product_id         VARCHAR(100)  NOT NULL,
    external_product_name       VARCHAR(255),
    external_brand_name         VARCHAR(255),
    match_confidence            NUMERIC(5, 4) NOT NULL DEFAULT 0,
    match_status                VARCHAR(30)   NOT NULL,
    matched_by                  VARCHAR(30)   NOT NULL,
    match_reason                VARCHAR(500),
    created_at                  TIMESTAMP     NOT NULL,
    updated_at                  TIMESTAMP     NOT NULL,
    CONSTRAINT uk_product_external_mapping UNIQUE (
        external_source,
        external_product_id,
        product_id
        )
);

CREATE INDEX IF NOT EXISTS idx_product_external_mappings_product_id
    ON product_external_mappings (product_id);

CREATE INDEX IF NOT EXISTS idx_product_external_mappings_external
    ON product_external_mappings (external_source, external_product_id);

CREATE TABLE IF NOT EXISTS product_external_prices (
    product_external_price_id BIGSERIAL PRIMARY KEY,
    product_id                BIGINT       NOT NULL REFERENCES products (product_id),
    external_source           VARCHAR(30)  NOT NULL,
    external_product_id       VARCHAR(100) NOT NULL,
    external_store_id         VARCHAR(100),
    price                     INTEGER      NOT NULL,
    inspected_date            DATE         NOT NULL,
    created_at                TIMESTAMP    NOT NULL,
    updated_at                TIMESTAMP    NOT NULL,
    CONSTRAINT uk_product_external_price UNIQUE (
    external_source,
    external_product_id,
    external_store_id,
    inspected_date
    )
);

CREATE INDEX IF NOT EXISTS idx_product_external_prices_product_id
    ON product_external_prices (product_id);