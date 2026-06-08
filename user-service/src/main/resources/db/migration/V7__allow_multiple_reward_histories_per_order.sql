ALTER TABLE rewards
    DROP CONSTRAINT IF EXISTS uk_rewards_order_id;

CREATE UNIQUE INDEX IF NOT EXISTS uk_rewards_order_id_reward_type
    ON rewards (order_id, reward_type)
    WHERE order_id IS NOT NULL;
