-- liquibase formatted sql
-- changeset ScherbachenyaMIK:create-table-Tags
CREATE TABLE Tags (
    tag_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    tag_name VARCHAR(255) NOT NULL UNIQUE
);
