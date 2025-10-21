CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) not null,
    surname VARCHAR(50) not null,
    birth_date DATE,
    email VARCHAR(100) not null
);

CREATE INDEX idx_users_email on users(email);
CREATE INDEX idx_users_name_surname on users(name, surname);