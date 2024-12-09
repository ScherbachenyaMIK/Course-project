-- liquibase formatted sql
-- changeset ScherbachenyaMIK:create-table-Moderators
CREATE TABLE Moderators (
    moderator_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);
