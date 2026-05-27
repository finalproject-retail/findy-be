-- beacons: major / minor / mac
-- 실물 Minew 4대 = README_physical_beacons.md
-- 나머지 518 = 가상 iBeacon 형식 (major=40011, minor=20000+grid_id, mac=C3:00:01:..)

ALTER TABLE beacons
    ADD COLUMN major INTEGER,
    ADD COLUMN minor INTEGER,
    ADD COLUMN mac VARCHAR(17);

-- 가상 518 (grid_id NOT IN 실물 4): 실기기와 비슷한 규칙으로 생성
UPDATE beacons
SET
    major = 40011,
    minor = 20000 + grid_id,
    mac = format(
        'C3:00:01:%s:%s:%s',
        lpad(to_hex(((grid_id / 256) % 256)::int), 2, '0'),
        lpad(to_hex((grid_id >> 8)::int), 2, '0'),
        lpad(to_hex((grid_id & 255)::int), 2, '0')
    )
WHERE store_id = 1
  AND grid_id NOT IN (43, 321, 333, 466);

-- 실물 Minew 4대: minor → grid_id = 56325→43, 56338→321, 56321→333, 56337→466
UPDATE beacons
SET major = 40011, minor = 56325, mac = 'C3:00:00:3F:45:1A'
WHERE store_id = 1 AND grid_id = 43;

UPDATE beacons
SET major = 40011, minor = 56338, mac = 'C3:00:00:3F:45:27'
WHERE store_id = 1 AND grid_id = 321;

UPDATE beacons
SET major = 40011, minor = 56321, mac = 'C3:00:00:3F:45:16'
WHERE store_id = 1 AND grid_id = 333;

UPDATE beacons
SET major = 40011, minor = 56337, mac = 'C3:00:00:3F:45:26'
WHERE store_id = 1 AND grid_id = 466;

CREATE UNIQUE INDEX idx_beacons_store_minor
    ON beacons (store_id, minor)
    WHERE minor IS NOT NULL;
