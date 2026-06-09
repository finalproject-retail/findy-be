-- grid_id 199, 200, 344, 345 (grid_x=24|25, grid_y=6|11)는 매대(SHELF)인데 AISLE로 잘못 시드됨.
-- 경로 탐색이 매대를 통과하지 않도록 수정.
UPDATE grids
SET cell_type = 'SHELF', updated_at = NOW()
WHERE store_id = 1
  AND (
    (grid_x = 24 AND grid_y = 6)
    OR (grid_x = 25 AND grid_y = 6)
    OR (grid_x = 24 AND grid_y = 11)
    OR (grid_x = 25 AND grid_y = 11)
  );
