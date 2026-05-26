-- store_id=1 (FINDY 강남점 + floor map); grids/beacons in V3/V4
-- store_maps width/height = 격자 수 × 5m (29×5=145, 18×5=90)

INSERT INTO stores (store_id, store_name, address, status, created_at, updated_at)
VALUES (1, 'FINDY 강남점', '서울특별시 강남구 테헤란로 123', 'ACTIVE', NOW(), NOW());

INSERT INTO store_maps (
    map_id, store_id, map_image_url, width, height, start_date, end_date, created_at, updated_at
)
VALUES (
    1,
    1,
    'https://example.com/maps/gangnam-floor1.png',
    145,
    90,
    DATE '2026-01-01',
    DATE '2029-12-31',
    NOW(),
    NOW()
);

SELECT setval(pg_get_serial_sequence('stores', 'store_id'), (SELECT COALESCE(MAX(store_id), 1) FROM stores));
SELECT setval(pg_get_serial_sequence('store_maps', 'map_id'), (SELECT COALESCE(MAX(map_id), 1) FROM store_maps));
