-- liquibase formatted sql
-- changeset ScherbachenyaMIK:create-table-Articles_Tags
CREATE TABLE Articles_Tags (
    tag_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    PRIMARY KEY (tag_id, article_id),
    CONSTRAINT fk_tag FOREIGN KEY (tag_id) REFERENCES Tags (tag_id) ON DELETE CASCADE,
    CONSTRAINT fk_article FOREIGN KEY (article_id) REFERENCES Articles (article_id) ON DELETE CASCADE
);

