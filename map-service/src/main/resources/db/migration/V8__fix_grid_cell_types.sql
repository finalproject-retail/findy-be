-- grids.cell_type 오기입 수정 (store_id=1)
-- SHELF → AISLE: 통로로 잘못 표시된 매대 격자
-- AISLE → SHELF: 매대로 잘못 표시된 통로 격자

UPDATE grids
SET cell_type = 'AISLE', updated_at = NOW()
WHERE store_id = 1
  AND cell_type = 'SHELF'
  AND grid_id IN (
    82, 83, 84, 85,
    169, 170, 171, 172,
    227, 228, 229, 230, 233,
    262, 291,
    314, 315, 316, 317, 320,
    372, 373, 374, 375,
    436, 495
  );

UPDATE grids
SET cell_type = 'SHELF', updated_at = NOW()
WHERE store_id = 1
  AND cell_type = 'AISLE'
  AND grid_id IN (430, 431, 432, 433);
