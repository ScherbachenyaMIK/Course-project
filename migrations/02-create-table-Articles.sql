-- liquibase formatted sql
-- changeset ScherbachenyaMIK:create-table-Articles
CREATE TABLE Articles (
    article_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    author_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    text_content TEXT NOT NULL,
    time_to_read INT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    visibility BOOLEAN DEFAULT TRUE,
    status VARCHAR(50) DEFAULT 'draft',
    views INT DEFAULT 0,
    likes INT DEFAULT 0,
    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES Users(user_id) ON DELETE CASCADE
);
