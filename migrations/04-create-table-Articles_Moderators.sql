-- liquibase formatted sql
-- changeset ScherbachenyaMIK:create-table-Articles_Moderators
CREATE TABLE Articles_Moderators (
    article_id BIGINT NOT NULL,
    moderator_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, moderator_id),
    CONSTRAINT fk_article FOREIGN KEY (article_id) REFERENCES Articles(article_id) ON DELETE CASCADE,
    CONSTRAINT fk_moderator FOREIGN KEY (moderator_id) REFERENCES Moderators(moderator_id) ON DELETE CASCADE
);
