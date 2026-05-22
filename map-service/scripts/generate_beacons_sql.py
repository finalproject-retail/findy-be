from pathlib import Path
import uuid

STORE_ID = 1
GRID_COUNT = 29 * 18  # 522, same order as seed_grids_store_1.sql (y outer, x inner)
BEACON_NAMESPACE = uuid.UUID("550e8400-e29b-41d4-a716-446655440000")


def beacon_uuid_for(grid_id: int) -> str:
    return str(uuid.uuid5(BEACON_NAMESPACE, f"store-{STORE_ID}-grid-{grid_id}"))


def main() -> None:
    values = []
    for grid_id in range(1, GRID_COUNT + 1):
        beacon_uuid = beacon_uuid_for(grid_id)
        values.append(
            f"({grid_id}, {STORE_ID}, {grid_id}, '{beacon_uuid}', NOW(), NOW())"
        )

    lines = [
        "-- store_id=1, one beacon per grid (522 rows)",
        "-- beacon_id = grid_id (1..522); run seed_grids_store_1.sql first",
        "-- grid_id assignment must match grid insert order (y:0..17, x:0..28)",
        "INSERT INTO beacons (beacon_id, store_id, grid_id, beacon_uuid, created_at, updated_at) VALUES",
        ",\n".join(values) + ";",
        "",
        "SELECT setval(pg_get_serial_sequence('beacons', 'beacon_id'), (SELECT MAX(beacon_id) FROM beacons));",
    ]

    output = Path(__file__).resolve().parent / "seed_beacons_store_1.sql"
    output.write_text("\n".join(lines), encoding="utf-8")
    print(f"written {output} ({len(values)} rows)")


if __name__ == "__main__":
    main()
