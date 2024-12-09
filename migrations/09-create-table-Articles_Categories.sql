-- liquibase formatted sql
-- changeset ScherbachenyaMIK:create-table-Articles_Categories
CREATE TABLE Articles_Categories (
    category_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    PRIMARY KEY (category_id, article_id),
    FOREIGN KEY (category_id) REFERENCES Categories(category_id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES Articles(article_id) ON DELETE CASCADE
);
