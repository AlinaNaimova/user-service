INSERT INTO card_info (id, user_id, number, holder, expiration_date)
VALUES
    (1, 1, '1111222233334444', 'Test User', '12/25'),
    (2, 1, '5555666677778888', 'Test User', '06/24'),
    (3, 2, '9999888877776666', 'Jane Smith', '03/26');

SELECT setval('card_info_id_seq', COALESCE((SELECT MAX(id) FROM card_info), 1));