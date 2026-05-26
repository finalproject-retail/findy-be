-- user_grades + dev sample users

INSERT INTO user_grades (grade_id, grade_name, criteria_amount, reward_rate, created_at, updated_at)
VALUES
    (1, 'BRONZE', 0, 0.1, NOW(), NOW()),
    (2, 'SILVER', 1200000, 0.5, NOW(), NOW()),
    (3, 'GOLD', 3600000, 1, NOW(), NOW()),
    (4, 'VIP', 6000000, 2, NOW(), NOW());

INSERT INTO users (
    user_id, email, password, name, phone_number, birth_date, gender, role,
    grade_id, deleted_at, reward, purchase_amount, is_first_login, created_at, updated_at
)
VALUES
    (
        1,
        'user@example.com',
        '$2a$10$bJmlRnOI0o.MWDZcTKjdkekfnZFQfqt.rRdEpeJ2qEZ7rBaWirc8C',
        '홍길동',
        '010-1234-5678',
        DATE '1995-05-15',
        'MALE',
        'ROLE_USER',
        1,
        NULL,
        0,
        0,
        TRUE,
        TIMESTAMP '2026-05-20 15:56:47.074868',
        TIMESTAMP '2026-05-20 15:56:47.074868'
    ),
    (
        2,
        'test@gmail.com',
        '$2a$10$obwgReMZs190Lw1uTA1VuOWCguQhKoR78z0.GdM7gAtIdTD1zsQSq',
        '테스트',
        '010-0000-0000',
        DATE '2000-05-23',
        'FEMALE',
        'ROLE_USER',
        1,
        NULL,
        0,
        0,
        TRUE,
        TIMESTAMP '2026-05-23 07:33:50.469872',
        TIMESTAMP '2026-05-23 07:33:50.469872'
    );

SELECT setval(pg_get_serial_sequence('user_grades', 'grade_id'), (SELECT COALESCE(MAX(grade_id), 1) FROM user_grades));
SELECT setval(pg_get_serial_sequence('users', 'user_id'), (SELECT COALESCE(MAX(user_id), 1) FROM users));
