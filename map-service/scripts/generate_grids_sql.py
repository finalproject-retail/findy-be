from pathlib import Path

WIDTH, HEIGHT, STORE_ID = 29, 18, 1
VERTICAL_SHELF_X = {2, 3, 5, 6, 8, 9, 11, 12, 14, 15, 17, 18, 20, 21}
SHELF_Y_BANDS = [(2, 5), (7, 10), (12, 15)]
RIGHT_SHELF_X = {23, 24, 25, 26}
RIGHT_SHELF_Y = {2, 3, 4, 5, 7, 8, 9, 10, 12, 13}


def is_border(x: int, y: int) -> bool:
    return x == 0 or x == WIDTH - 1 or y == 0 or y == HEIGHT - 1


def is_interior_shelf(x: int, y: int) -> bool:
    if x in VERTICAL_SHELF_X and any(lo <= y <= hi for lo, hi in SHELF_Y_BANDS):
        return True
    if x in RIGHT_SHELF_X and y in RIGHT_SHELF_Y:
        return True
    return False


def resolve(x: int, y: int) -> str:
    if is_border(x, y) or is_interior_shelf(x, y):
        return "SHELF"
    return "AISLE"


def main() -> None:
    values = []
    for y in range(HEIGHT):
        for x in range(WIDTH):
            cell_type = resolve(x, y)
            values.append(
                f"({STORE_ID}, {x}, {y}, '{cell_type}', NOW(), NOW())"
            )

    assert len(values) == 522

    lines = [
        "-- store_id=1, grid 29x18 = 522 cells",
        "-- SHELF: walls/shelves, AISLE: walkable (pathfinding uses AISLE only)",
        "INSERT INTO grids (store_id, grid_x, grid_y, cell_type, created_at, updated_at) VALUES",
        ",\n".join(values) + ";",
    ]

    output = Path(__file__).resolve().parent / "seed_grids_store_1.sql"
    output.write_text("\n".join(lines), encoding="utf-8")
    print(f"written {output} ({len(values)} rows)")


if __name__ == "__main__":
    main()
