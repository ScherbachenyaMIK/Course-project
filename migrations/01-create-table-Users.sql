-- liquibase formatted sql
-- changeset ScherbachenyaMIK:create-table-Users
CREATE TABLE Users (
    user_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    user_role VARCHAR(50),
    sex CHAR(1),
    birth_date DATE
);
