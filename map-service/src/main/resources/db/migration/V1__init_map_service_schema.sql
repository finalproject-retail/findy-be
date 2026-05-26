-- map-service initial schema (matches JPA entities)

CREATE TABLE stores (
    store_id    BIGSERIAL PRIMARY KEY,
    store_name  VARCHAR(100)  NOT NULL,
    address     VARCHAR(255)  NOT NULL,
    status      VARCHAR(30)   NOT NULL,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);

CREATE TABLE store_maps (
    map_id          BIGSERIAL PRIMARY KEY,
    store_id        BIGINT        NOT NULL REFERENCES stores (store_id),
    map_image_url   VARCHAR(500),
    width           INTEGER       NOT NULL,
    height          INTEGER       NOT NULL,
    start_date      DATE,
    end_date        DATE,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL
);

CREATE TABLE grids (
    grid_id     BIGSERIAL PRIMARY KEY,
    store_id    BIGINT        NOT NULL REFERENCES stores (store_id),
    grid_x      INTEGER       NOT NULL,
    grid_y      INTEGER       NOT NULL,
    cell_type   VARCHAR(30)   NOT NULL,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);

CREATE TABLE beacons (
    beacon_id    BIGSERIAL PRIMARY KEY,
    store_id     BIGINT        NOT NULL REFERENCES stores (store_id),
    grid_id      BIGINT        NOT NULL REFERENCES grids (grid_id),
    beacon_uuid  VARCHAR(255)  NOT NULL,
    created_at   TIMESTAMP     NOT NULL,
    updated_at   TIMESTAMP     NOT NULL
);

CREATE TABLE beacon_signal_logs (
    beacon_signal_log_id  BIGSERIAL PRIMARY KEY,
    user_id               BIGINT        NOT NULL,
    store_id              BIGINT        NOT NULL REFERENCES stores (store_id),
    beacon_id             BIGINT        NOT NULL REFERENCES beacons (beacon_id),
    timestamp_iso         TIMESTAMPTZ,
    mac                   VARCHAR(17),
    bluetooth_address_hex VARCHAR(14),
    rssi                  INTEGER,
    uuid                  VARCHAR(36),
    major                 INTEGER,
    minor                 INTEGER,
    tx                    INTEGER,
    nearest_grid_id       BIGINT        REFERENCES grids (grid_id),
    created_at            TIMESTAMP     NOT NULL,
    updated_at            TIMESTAMP     NOT NULL
);

CREATE INDEX idx_store_maps_store_id ON store_maps (store_id);
CREATE INDEX idx_grids_store_id ON grids (store_id);
CREATE INDEX idx_grids_store_xy ON grids (store_id, grid_x, grid_y);
CREATE INDEX idx_beacons_store_id ON beacons (store_id);
CREATE INDEX idx_beacons_grid_id ON beacons (grid_id);
CREATE INDEX idx_beacon_signal_logs_store_id ON beacon_signal_logs (store_id);
CREATE INDEX idx_beacon_signal_logs_beacon_id ON beacon_signal_logs (beacon_id);
CREATE INDEX idx_beacon_signal_logs_user_id ON beacon_signal_logs (user_id);
CREATE INDEX idx_beacon_signal_logs_timestamp_iso ON beacon_signal_logs (timestamp_iso);
