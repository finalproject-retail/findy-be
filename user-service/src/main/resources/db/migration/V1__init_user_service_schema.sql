-- user-service initial schema (matches JPA entities)

CREATE TABLE user_grades (
    grade_id         BIGSERIAL PRIMARY KEY,
    grade_name       VARCHAR(255)  NOT NULL,
    criteria_amount  BIGINT        NOT NULL,
    reward_rate      DOUBLE PRECISION NOT NULL,
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL
);

CREATE TABLE users (
    user_id          BIGSERIAL PRIMARY KEY,
    email            VARCHAR(255),
    password         VARCHAR(255),
    name             VARCHAR(255)  NOT NULL,
    phone_number     VARCHAR(255)  NOT NULL,
    birth_date       DATE          NOT NULL,
    gender           VARCHAR(255)  NOT NULL,
    role             VARCHAR(255)  NOT NULL,
    grade_id         BIGINT        NOT NULL REFERENCES user_grades (grade_id),
    deleted_at       TIMESTAMP,
    reward           DOUBLE PRECISION NOT NULL,
    purchase_amount  BIGINT        NOT NULL,
    is_first_login   BOOLEAN       NOT NULL,
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE shopping_styles (
    shopping_style_id BIGSERIAL PRIMARY KEY,
    style_name        VARCHAR(255) NOT NULL,
    is_active         BOOLEAN      NOT NULL
);

CREATE TABLE user_preferred_categories (
    user_preferred_category_id BIGSERIAL PRIMARY KEY,
    user_id                    BIGINT NOT NULL REFERENCES users (user_id),
    category_id                BIGINT NOT NULL
);

CREATE TABLE user_shopping_styles (
    user_shopping_style_id BIGSERIAL PRIMARY KEY,
    user_id                BIGINT NOT NULL REFERENCES users (user_id),
    shopping_style_id      BIGINT NOT NULL REFERENCES shopping_styles (shopping_style_id)
);

CREATE INDEX idx_users_grade_id ON users (grade_id);
CREATE INDEX idx_user_preferred_categories_user_id ON user_preferred_categories (user_id);
CREATE INDEX idx_user_shopping_styles_user_id ON user_shopping_styles (user_id);
