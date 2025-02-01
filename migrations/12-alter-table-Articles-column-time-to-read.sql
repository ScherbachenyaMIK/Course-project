-- liquibase formatted sql
-- changeset ScherbachenyaMIK:alter-articles-time-to-read-default
ALTER TABLE Articles ALTER COLUMN time_to_read SET DEFAULT 30;
