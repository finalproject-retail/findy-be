-- recommendation-service initial schema

CREATE TABLE IF NOT EXISTS products (
    product_id               BIGINT PRIMARY KEY,
    category_id              BIGINT         NOT NULL,
    brand_name               VARCHAR(100),
    product_name             VARCHAR(255)   NOT NULL,
    original_price           INTEGER        NOT NULL,
    sale_price               INTEGER        NOT NULL,
    discount_rate            NUMERIC(5, 2)  NOT NULL,
    description              TEXT,
    image_url                VARCHAR(500),
    packaging_type           VARCHAR(100),
    sales_unit               VARCHAR(100),
    volume                   VARCHAR(100),
    allergy_info             TEXT,
    badge_text               VARCHAR(100),
    sale_status              VARCHAR(255)   NOT NULL,
    is_deleted               BOOLEAN        NOT NULL
);

CREATE TABLE IF NOT EXISTS inventories (
    inventory_id    BIGINT PRIMARY KEY,
    product_id      BIGINT        NOT NULL,
    store_id        BIGINT        NOT NULL,
    stock_quantity  INTEGER       NOT NULL,
    unit            VARCHAR(30)   NOT NULL,
    stock_status    VARCHAR(255)  NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    category_id         BIGINT PRIMARY KEY,
    parent_category_id  BIGINT,
    category_name       VARCHAR(255) NOT NULL,
    is_active           BOOLEAN      NOT NULL
);

CREATE TABLE IF NOT EXISTS shopping_styles (
    shopping_style_id BIGINT PRIMARY KEY,
    style_name        VARCHAR(255) NOT NULL,
    is_active         BOOLEAN      NOT NULL
);

CREATE TABLE IF NOT EXISTS user_preferred_categories (
    user_preferred_category_id BIGINT PRIMARY KEY,
    user_id                    BIGINT NOT NULL,
    category_id                BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS user_shopping_styles (
    user_shopping_style_id BIGINT PRIMARY KEY,
    user_id                BIGINT NOT NULL,
    shopping_style_id      BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS product_embeddings (
    product_embedding_id BIGSERIAL PRIMARY KEY,
    product_id           BIGINT       NOT NULL,
    model                VARCHAR(255) NOT NULL,
    dimensions           INTEGER      NOT NULL,
    embedding            TEXT         NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    CONSTRAINT uk_product_embeddings_product_id UNIQUE (product_id)
);

CREATE INDEX IF NOT EXISTS idx_inventories_product_id ON inventories (product_id);
CREATE INDEX IF NOT EXISTS idx_user_preferred_categories_user_id ON user_preferred_categories (user_id);
CREATE INDEX IF NOT EXISTS idx_user_shopping_styles_user_id ON user_shopping_styles (user_id);
CREATE INDEX IF NOT EXISTS idx_product_embeddings_product_id ON product_embeddings (product_id);
