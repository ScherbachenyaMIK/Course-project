-- liquibase formatted sql
-- changeset ScherbachenyaMIK:alter-table-Articles-add-search-vector
ALTER TABLE articles
    ADD COLUMN search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('russian', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('russian', coalesce(text_content, '')), 'B')
    ) STORED;

CREATE INDEX idx_articles_search_vector ON articles USING GIN (search_vector);
