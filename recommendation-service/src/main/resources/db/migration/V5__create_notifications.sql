CREATE TABLE IF NOT EXISTS notifications (
                                             notification_id       BIGSERIAL PRIMARY KEY,
                                             user_id               BIGINT       NOT NULL,
                                             notification_type     VARCHAR(50)  NOT NULL,
    title                 VARCHAR(255) NOT NULL,
    content               VARCHAR(500) NOT NULL,
    is_read               BOOLEAN      NOT NULL DEFAULT FALSE,
    product_id            BIGINT,
    source_product_id     BIGINT,
    promotion_id          BIGINT,
    recommendation_log_id BIGINT,
    store_id              BIGINT,
    shopping_list_id      BIGINT,
    display_position      VARCHAR(50)  NOT NULL,
    sent_at               TIMESTAMP    NOT NULL,
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_notifications_user_id_sent_at
    ON notifications (user_id, sent_at DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_user_product_type_sent_at
    ON notifications (user_id, product_id, notification_type, sent_at);

CREATE INDEX IF NOT EXISTS idx_notifications_user_shopping_list_product_type
    ON notifications (user_id, shopping_list_id, product_id, notification_type);