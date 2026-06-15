CREATE TABLE IF NOT EXISTS kafka_processed_events (
    event_id VARCHAR(100) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_kafka_processed_events_type_processed_at
    ON kafka_processed_events (event_type, processed_at);
