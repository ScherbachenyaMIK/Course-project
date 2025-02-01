-- liquibase formatted sql
-- changeset ScherbachenyaMIK:alter-author-id-type
ALTER TABLE Articles ALTER COLUMN author_id TYPE BIGINT;
