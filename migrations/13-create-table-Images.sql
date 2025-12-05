-- liquibase formatted sql
-- changeset ScherbachenyaMIK:create-tables-for-store-images
CREATE TABLE mime_types (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO mime_types (type) VALUES
('image/jpeg'),       -- .jpg, .jpeg
('image/pjpeg'),      -- устаревший progressive JPEG, но всё ещё встречается
('image/png'),        -- .png
('image/gif'),        -- .gif
('image/webp'),       -- .webp
('image/bmp'),        -- .bmp
('image/x-windows-bmp'), -- .bmp
('image/vnd.microsoft.icon'), -- .ico
('image/x-icon'),     -- .ico
('image/svg+xml'),    -- .svg
('image/heic'),       -- .heic
('image/heif'),       -- .heif
('image/tiff'),       -- .tif, .tiff
('image/avif');       -- .avif


CREATE TABLE images (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    path TEXT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    mime_type_id INT NOT NULL REFERENCES mime_types(id),
    content BYTEA NOT NULL
);

CREATE INDEX idx_images_path ON images (path);
