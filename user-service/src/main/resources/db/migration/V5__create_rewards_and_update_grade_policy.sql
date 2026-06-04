ALTER TABLE users
    ALTER COLUMN reward TYPE BIGINT USING FLOOR(reward)::BIGINT,
    ALTER COLUMN reward SET DEFAULT 0,
    ALTER COLUMN purchase_amount SET DEFAULT 0;

CREATE TABLE IF NOT EXISTS rewards
(
    reward_history_id BIGSERIAL PRIMARY KEY,
    user_id           BIGINT      NOT NULL REFERENCES users (user_id),
    order_id          BIGINT,
    reward_type       VARCHAR(30) NOT NULL,
    reward_amount     BIGINT      NOT NULL,
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP   NOT NULL,
    CONSTRAINT uk_rewards_order_id UNIQUE (order_id)
);

CREATE INDEX IF NOT EXISTS idx_rewards_user_id
    ON rewards (user_id);

CREATE INDEX IF NOT EXISTS idx_rewards_order_id
    ON rewards (order_id);

UPDATE user_grades
SET criteria_amount = CASE grade_name
                          WHEN 'BRONZE' THEN 0
                          WHEN 'SILVER' THEN 1000000
                          WHEN 'GOLD' THEN 2000000
                          WHEN 'VIP' THEN 3000000
                          ELSE criteria_amount
    END,
    reward_rate     = CASE grade_name
                          WHEN 'BRONZE' THEN 0.5
                          WHEN 'SILVER' THEN 1.0
                          WHEN 'GOLD' THEN 1.5
                          WHEN 'VIP' THEN 2.0
                          ELSE reward_rate
        END,
    updated_at      = now()
WHERE grade_name IN ('BRONZE', 'SILVER', 'GOLD', 'VIP');