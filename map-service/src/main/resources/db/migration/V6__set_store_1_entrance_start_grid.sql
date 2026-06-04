-- 좌측 출입구 (grid_x=0, grid_y=16|17 → grid_id 465, 494). V3가 SHELF로 넣은 칸만 START로 변경.
UPDATE grids
SET cell_type = 'START', updated_at = NOW()
WHERE store_id = 1 AND grid_x = 0 AND grid_y IN (16, 17);
