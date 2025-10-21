CREATE TABLE card_info (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    number VARCHAR(19) not null,
    holder VARCHAR(50) not null,
    expiration_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_card_info_user_id on card_info(user_id);
CREATE INDEX idx_card_info_number on card_info(number);