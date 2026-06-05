-- Admin account for management login (ROLE_ADMIN)
-- email: admin@findy.com
-- password: admin1234

INSERT INTO users (
    email,
    password,
    name,
    phone_number,
    birth_date,
    gender,
    role,
    grade_id,
    deleted_at,
    reward,
    purchase_amount,
    is_first_login,
    created_at,
    updated_at
)
VALUES (
    'admin@findy.com',
    '$2a$10$5C93JFpv18ffu8.SbgkAU.FIqhxoWf3pyl/pc3YU0QDHEStcaZ0Oi',
    '관리자',
    '010-9999-0001',
    '1990-01-01',
    'MALE',
    'ROLE_ADMIN',
    1,
    NULL,
    0,
    0,
    FALSE,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('users', 'user_id'),
    (SELECT COALESCE(MAX(user_id), 1) FROM users)
);
