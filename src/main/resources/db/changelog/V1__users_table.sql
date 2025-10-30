CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) not null,
    surname VARCHAR(50) not null,
    birth_date DATE NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE INDEX idx_users_email on users(email);