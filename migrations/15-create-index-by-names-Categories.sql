-- liquibase formatted sql
-- changeset ScherbachenyaMIK:add-category-name-index
CREATE INDEX idx_category_name ON categories(category_name);
