You are an expert backend developer working with a system that already has a defined storage schema for images and a `ResourceController` serving existing image retrieval endpoints. Your task is to extend the system by implementing a robust **upload API** for three distinct image categories.

## 1. Context and Existing System State

**Database Schema:**
The system relies on the following two PostgreSQL tables:

```sql
CREATE TABLE mime_types (
    id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    type VARCHAR(255) NOT NULL UNIQUE -- e.g., 'image/jpeg', 'image/png'
);

CREATE TABLE images (
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    path          TEXT NOT NULL,           -- Logical path e.g., '/user_icon/123'
    filename      VARCHAR(255) NOT NULL,           -- Original filename
    mime_type_id  INT NOT NULL REFERENCES mime_types(id),
    content       BYTEA NOT NULL
);
```

**Existing Retrieval Endpoints (`ResourceController`):**
The following endpoints are already implemented for serving images from the `images` table based on their logical `path`:

- `GET /api/resources/preview/{articleId}` -> Method: `getPreviewImage` -> Logical path: `/preview/{articleId}`
- `GET /api/resources/user-icon/{userId}` -> Method: `getUserIcon` -> Logical path: `/user_icon/{userId}`
- `GET /api/resources/article/{articleId}/images/{imageId}` -> Method: `getArticleIcon` -> Logical path: `/article/{articleId}/images/{imageId}`

**Architecture Note:**
Communication between the **Backend** service (where this API resides) and the **Scrapper** service (which processes and optimizes images) must **bypass Kafka** for the actual binary transfer. Instead, it must use the existing **`ScrapperClient`** (a Spring `WebClient` bean) to send the file directly.

## 2. Task Requirements

You are to prepare the environment and implement the logic for **uploading** images that correspond to the three existing retrieval patterns.

### Phase 1: Environment & Validation Setup
1.  **MIME Type Seeder:** Explore database `mime_types` table to find formats
2.  **Validation Logic:** Implement a service method `validateAndGetMimeType(MultipartFile file)` that checks the uploaded file's MIME type against the `mime_types` table. Throw a clear `UnsupportedMediaTypeException` if the type is not in the table.

### Phase 2: API Implementation
Create a new REST Controller (`ImageUploadController`) with three distinct POST endpoints. For each endpoint, follow the exact logic flow described below.

**Endpoint 1: Upload User Icon**
- **URL:** `POST /api/resources/user-icon/{userId}`
- **Request:** `MultipartFile`
- **Logical Path Mapping:** `/user_icon/{userId}` (This path must be **unique**. Ensure a new upload **replaces** any existing record with the same `path`).
- **Action:** Validate MIME type -> Save to DB -> Trigger Scrapper async process.

**Endpoint 2: Upload Article Preview**
- **URL:** `POST /api/resources/preview/{articleId}`
- **Request:** `MultipartFile`
- **Logical Path Mapping:** `/preview/{articleId}`
- **Action:** Validate MIME type -> Save to DB (replace existing path if exists) -> Trigger Scrapper async process.

**Endpoint 3: Upload Article Content Image**
- **URL:** `POST /api/resources/article/{articleId}/images`
- **Request:** `MultipartFile`
- **Logical Path Mapping:** `/article/{articleId}/images/{generated_image_id}` (Note: This is **not** a replacement. Each upload creates a **new** image record with a new unique ID in the path).
- **Action:** Validate MIME type -> Save to DB as **new** record -> Trigger Scrapper async process -> Return `201 Created` with the `Location` header pointing to the new `imageId`.

### Phase 3: Integration with Scrapper Service (Bypassing Kafka)
1.  **ScrapperClient Usage:** Do **not** use Kafka events for the image payload. Use the existing `ScrapperClient` (WebClient) to send a `POST` request to the Scrapper service endpoint (e.g., `/internal/process`).
2.  **Payload:** The request to Scrapper should include:
    - `imageId` (Database ID)
    - `originalFilename`
    - `mimeType`
    - `targetPath` (The logical path string)
3.  **Async Handling:** The database save operation must be fast and synchronous. The call to `ScrapperClient` should be **non-blocking** (e.g., using `@Async`, `Mono`, or `CompletableFuture`). The API response to the user must **not** wait for the Scrapper to finish processing.

### Phase 4: Repository Logic
Implement `ImageRepository` with the following capabilities:
- `save(Image image)`: Standard save.
- `findByPath(String path)`: Used to check for existing user icons and previews.
- `deleteByPath(String path)`: Used before inserting a replacement for user icon or preview (or use an `UPSERT` logic).

## 3. Expected Output Format
Provide a complete, production-ready code implementation in **Java (Spring Boot 3.x)** . Structure your answer as follows:

1.  **Entity Classes:** `MimeType` and `Image` with JPA annotations.
2.  **Repository:** `ImageRepository` interface.
3.  **Service Layer:** `ImageStorageService` (handles validation, DB operations, and calling `ScrapperClient`).
4.  **Controller:** `ImageUploadController` with the three endpoints.
5.  **Configuration:** Any necessary `WebClient` configuration or `@Async` executor setup.

**Crucial Constraint:** Ensure the `ResourceController` retrieval methods will work seamlessly with the data inserted by your new upload API (matching the `path` structure exactly).