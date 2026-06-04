package com.retail.map_service.domain.path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.retail.map_service.domain.StoreMapLayout;
import com.retail.map_service.entity.GridCellType;
import com.retail.map_service.entity.GridEntity;

public final class StoreGridMap {

	private final int cols;
	private final int rows;
	private final Map<Long, GridEntity> gridById;
	private final Map<String, Long> gridIdByCoordinateKey;

	public StoreGridMap(List<GridEntity> grids) {
		this.cols = StoreMapLayout.GRID_COLS;
		this.rows = StoreMapLayout.GRID_ROWS;
		this.gridById = new HashMap<>();
		this.gridIdByCoordinateKey = new HashMap<>();

		for (GridEntity grid : grids) {
			gridById.put(grid.getGridId(), grid);
			gridIdByCoordinateKey.put(coordinateKey(grid.getGridX(), grid.getGridY()), grid.getGridId());
		}
	}

	public int cols() {
		return cols;
	}

	public int rows() {
		return rows;
	}

	public GridEntity getGrid(Long gridId) {
		return gridById.get(gridId);
	}

	public Long gridIdAt(int gridX, int gridY) {
		return gridIdByCoordinateKey.get(coordinateKey(gridX, gridY));
	}

	/** {@code grids.cell_type}이 START 또는 AISLE이면 통행 가능. */
	public boolean isWalkable(Long gridId) {
		GridEntity grid = gridById.get(gridId);
		if (grid == null) {
			return false;
		}
		GridCellType cellType = grid.getCellType();
		return cellType == GridCellType.START || cellType == GridCellType.AISLE;
	}

	static String coordinateKey(int gridX, int gridY) {
		return gridX + "," + gridY;
	}
}
