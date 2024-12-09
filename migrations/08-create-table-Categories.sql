-- liquibase formatted sql
-- changeset ScherbachenyaMIK:create-table-Categories
CREATE TABLE Categories (
    category_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    category_name VARCHAR(255) UNIQUE NOT NULL,
    category_description TEXT NOT NULL
);
