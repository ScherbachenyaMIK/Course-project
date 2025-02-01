-- liquibase formatted sql
-- changeset ScherbachenyaMIK:alter-articles-visibility-default
ALTER TABLE Articles ALTER COLUMN visibility SET DEFAULT FALSE;
