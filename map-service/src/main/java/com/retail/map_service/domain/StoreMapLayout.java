package com.retail.map_service.domain;

/**
 * Flyway V3 시드와 동일: y 외부(0..17), x 내부(0..28) → grid_id = y * cols + x + 1
 */
public final class StoreMapLayout {

	public static final int GRID_COLS = 29;
	public static final int GRID_ROWS = 18;
	public static final int CELL_SIZE_METERS = 5;
	public static final String GRID_ID_FORMULA = "gridY * gridCols + gridX + 1";

	private StoreMapLayout() {
	}
}
