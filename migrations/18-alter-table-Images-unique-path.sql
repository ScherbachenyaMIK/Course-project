-- liquibase formatted sql
-- changeset ScherbachenyaMIK:images-path-unique
DROP INDEX IF EXISTS idx_images_path;
ALTER TABLE images ADD CONSTRAINT uq_images_path UNIQUE (path);
