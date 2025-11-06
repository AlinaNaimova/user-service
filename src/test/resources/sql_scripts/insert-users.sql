INSERT INTO users (id, name, surname, birth_date, email)
VALUES
    (1, 'Test', 'User', '1990-01-01', 'test.user@example.com'),
    (2, 'Jane', 'Smith', '1985-05-15', 'jane.smith@example.com');

SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 1));