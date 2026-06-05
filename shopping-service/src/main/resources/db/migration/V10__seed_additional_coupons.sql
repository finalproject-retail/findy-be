-- Additional coupon catalog seed (mockCoupons + 최소 주문 금액 할인)
-- 기존 V3 시드(10001~10002)와 ID 충돌 없음
-- coupon_type: ALL(전체 상품), PRODUCT(특정 상품), BRAND(특정 브랜드)

INSERT INTO coupons (
    coupon_id,
    coupon_name,
    coupon_type,
    discount_type,
    discount_value,
    is_stackable,
    min_order_amount,
    start_at,
    end_at,
    period_type,
    days_limit,
    is_active,
    created_at,
    updated_at
)
VALUES
    (
        10003,
        '[쌀 20kg] 농축산물할인지원 5천원 쿠폰',
        'PRODUCT',
        'AMOUNT',
        5000,
        FALSE,
        10000,
        '2026-02-01 00:00:00',
        '2026-05-31 23:59:59',
        'ABSOLUTE',
        NULL,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        10004,
        '[정육] 국내산 한우 3천원 할인',
        'PRODUCT',
        'AMOUNT',
        3000,
        FALSE,
        30000,
        '2026-06-01 00:00:00',
        '2026-06-07 23:59:59',
        'ABSOLUTE',
        NULL,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        10006,
        '[매일유업] 브랜드 전용 1,500원 할인',
        'BRAND',
        'AMOUNT',
        1500,
        TRUE,
        8000,
        '2026-05-22 00:00:00',
        '2026-06-22 23:59:59',
        'RELATIVE',
        10,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        10007,
        '스낵·과자 1천원 할인',
        'PRODUCT',
        'AMOUNT',
        1000,
        TRUE,
        5000,
        '2026-06-07 00:00:00',
        '2026-06-10 23:59:59',
        'ABSOLUTE',
        NULL,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        10008,
        '신규 가입 웰컴 5천원 쿠폰',
        'ALL',
        'AMOUNT',
        5000,
        FALSE,
        60000,
        '2026-01-01 00:00:00',
        '2026-12-31 23:59:59',
        'RELATIVE',
        30,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        10009,
        '2만원 이상 구매 시 3,000원 할인',
        'ALL',
        'AMOUNT',
        3000,
        FALSE,
        20000,
        '2026-06-04 00:00:00',
        '2026-06-05 23:59:59',
        'ABSOLUTE',
        NULL,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        10010,
        '3만원 이상 구매 시 10% 할인',
        'ALL',
        'RATE',
        10,
        FALSE,
        30000,
        '2026-06-04 00:00:00',
        '2026-06-07 23:59:59',
        'ABSOLUTE',
        NULL,
        TRUE,
        NOW(),
        NOW()
    ),
    (
        10011,
        '5만원 이상 구매 시 5,000원 할인',
        'ALL',
        'AMOUNT',
        5000,
        FALSE,
        50000,
        '2026-06-01 00:00:00',
        '2026-06-04 23:59:59',
        'ABSOLUTE',
        NULL,
        FALSE,
        NOW(),
        NOW()
    ),
    (
        10012,
        '10만원 이상 구매 시 15,000원 할인',
        'ALL',
        'AMOUNT',
        15000,
        FALSE,
        100000,
        '2026-06-01 00:00:00',
        '2026-06-11 23:59:59',
        'ABSOLUTE',
        NULL,
        TRUE,
        NOW(),
        NOW()
    )
ON CONFLICT (coupon_id) DO NOTHING;

-- 테스트 계정(user_id=1)에 바로 쓸 수 있도록 기본 쿠폰 지급
INSERT INTO user_coupons (
    user_id,
    coupon_id,
    is_used,
    downloaded_at,
    expires_at,
    used_at,
    created_at,
    updated_at
)
SELECT
    1,
    c.coupon_id,
    FALSE,
    NOW(),
    c.end_at,
    NULL,
    NOW(),
    NOW()
FROM coupons c
WHERE c.coupon_id IN (10003, 10004, 10006, 10008, 10009, 10010, 10011)
  AND NOT EXISTS (
      SELECT 1
      FROM user_coupons uc
      WHERE uc.user_id = 1
        AND uc.coupon_id = c.coupon_id
  );

SELECT setval(
    pg_get_serial_sequence('coupons', 'coupon_id'),
    COALESCE((SELECT MAX(coupon_id) FROM coupons), 1)
);

SELECT setval(
    pg_get_serial_sequence('user_coupons', 'user_coupon_id'),
    COALESCE((SELECT MAX(user_coupon_id) FROM user_coupons), 1)
);
