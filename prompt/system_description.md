# System Overview (Base Architecture)

The platform is a microservice web application for publishing author content (articles) with support for user roles, comments, moderation, and advanced search. Artificial intelligence (AI) is a separate add‑on (AI Gateway) and is not covered in this description.

## Main Components

**Backend** – a Spring Boot web server (port 8090). Responsible for:

- receiving HTTP requests from the browser;
- generating HTML pages via the Thymeleaf templating engine;
- sending messages to Apache Kafka and waiting for asynchronous responses;
- authentication/authorization (JWT + cookie);
- proxying requests to the Scrapper module for all operations that require database access or business logic.

**Scrapper** – a Spring Boot service (port 8080). The only component with direct access to PostgreSQL. Performs:

- business logic (creating/editing articles, comments, user management);
- consuming messages from Kafka sent by Backend;
- sending responses back via Kafka;
- database schema migrations (Liquibase).

**Kafka** – an asynchronous message bus (cluster of three brokers + Zookeeper). Used for reliable and scalable communication between Backend and Scrapper.

**PostgreSQL** – the main relational database (schema includes tables: Users, Articles, Comments, Tags, Categories, Moderators, many‑to‑many relationships).

**Docker / docker-compose** – containerisation of all services (PostgreSQL, Liquibase, Zookeeper, three Kafka brokers, Prometheus, Grafana, Thanos).

**CI/CD** – GitHub Actions (running tests, Checkstyle, JaCoCo, building, publishing images).

**Monitoring** – Prometheus (metric collection), Thanos (long‑term storage), Grafana (dashboards).

## Key Interaction Principles

- Backend never accesses the database directly – only through Scrapper.
- Requests that require processing (e.g., creating an article) are sent to Kafka. Backend stores a `CompletableFuture` in a map tied to a `correlationId`.
- Scrapper listens to topics, executes the logic, and sends a response to another topic with the same `correlationId`.
- Backend receives the response, finds the `CompletableFuture`, and returns the result to the user.
- Media files (images) are transmitted **bypassing Kafka** – through a special resource controller that streams BLOBs from the database and caches them at the Backend level.

## Use Cases (Happy Path) and Forks

### 1. New User Registration

**Happy path:**
1. User fills out the registration form (username, email, password) on the `/register` page.
2. Backend validates the data (email is unique, password not empty).
3. Backend sends a message to Kafka (topic `user_registration`) with a DTO containing the user data.
4. Scrapper receives the message, saves a record to the `Users` table with status `NOT_CONFIRMED` and the password as a hash.
5. Scrapper initiates sending a confirmation email to the given address (via external SMTP or mock service). The email contains a link like `/confirm?token=...`.
6. User follows the link → Backend sends a confirmation request to Scrapper.
7. Scrapper changes the user’s status to `USER` (or `AUTHOR`).
8. User receives a success notification and can log in.

**Forks:**
- Email already exists → Backend returns 409 Conflict.
- Invalid email / short password → validation error 400.
- Email not delivered (SMTP timeout) → the task may be retried (Kafka retries), but the user sees a message “The email may take a few minutes to arrive”.
- User does not confirm email → when attempting to log in, they receive an error “Account not confirmed”.

### 2. Authentication and Authorization

**Happy path:**
1. User enters username/password on the `/login` page.
2. Backend sends a request to Scrapper (via Kafka) for verification.
3. Scrapper checks that the user exists, the password hash matches, and the status is `USER` or `ADMIN`.
4. On success, Scrapper returns `userId` and role.
5. Backend generates a JWT token (contains `username`, `role`, validity 24 h) and sets it as an `HttpOnly` cookie.
6. User is redirected to the main page. On every subsequent request, `AuthenticationFilter` checks the JWT from the cookie and extracts the user.

**Forks:**
- Wrong username/password → 401 error, login form with a message.
- Account not confirmed → 403 error “Confirm your email”.
- JWT expired → filter returns 401, browser redirects to `/login`.
- Cookie missing or corrupted → similarly 401.

### 3. Creating a New Article

**Happy path:**
1. An authenticated author navigates to `/articles/new`.
2. Fills required fields: **title**, **brief description**, **content** (HTML/text), selects **categories** and **tags**.
3. Backend forms a DTO (`ArticleCreateRequest`) with a `correlationId` and sends it to the topic `article_create`.
4. Scrapper:
   - verifies that the author exists and has permissions;
   - saves the article in the `Articles` table with status `DRAFT` and visibility `HIDDEN`;
   - saves relationships with tags and categories;
   - sends a response with `articleId` and status `CREATED`.
5. Backend receives the response, shows a confirmation page with a “Publish” button.

**Forks:**
- Required field missing → 400 error, the form is reloaded with hints.
- Content exceeds maximum length → validation error.
- Kafka unavailable → Backend returns 503 Service Unavailable (with a suggestion to try again later).
- Scrapper does not respond within timeout → `CompletableFuture` completes exceptionally, user sees “Service temporarily unavailable”.

### 4. Publishing / Hiding an Article

**Happy path:**
1. Author opens their article in edit mode.
2. Clicks “Publish” (or “Hide”).
3. Backend sends a request to the topic `article_publish` with `articleId`.
4. Scrapper changes the `visibility` field to `PUBLIC` (or `HIDDEN`) and the `status` to `PUBLISHED` (if it was `DRAFT`).
5. After updating, Scrapper indexes the article in **ElasticSearch** (for full‑text search).
6. The response returns, and the article becomes available to all readers.

**Forks:**
- Article already published → repeated publication is ignored (Scrapper returns success without changes).
- When hiding an article, it is removed from the ElasticSearch index (or marked as hidden).
- Indexing error does not block the main operation but is logged in Scrapper.

### 5. Viewing and Reading Articles

**Happy path:**
1. Reader visits the main page `/` or a category page.
2. Backend sends a request to Scrapper (via Kafka) to obtain a filtered list of articles (by date, popularity, categories).
3. Scrapper queries the database (with JOINs on tags/categories) and returns a list of `ArticlePreviewDTO`.
4. Backend renders the page using Thymeleaf, inserting data into the HTML template.
5. Reader clicks on a title → request `/articles/{id}`. Scrapper returns the full article object (content, metadata, author, comments).
6. Backend displays the article page with buttons (edit – if author, moderate – if moderator/admin).

**Forks:**
- Article is hidden, and the reader is neither the author nor a moderator → Scrapper returns 404 Not Found.
- Article not found → Backend shows a 404 page.
- For large result sets, pagination (limit/offset) is used.

### 6. Commenting on an Article

**Happy path:**
1. An authenticated user enters a comment in the form below the article.
2. Backend sends a request to the topic `comment_create` with `articleId`, `userId`, and the text.
3. Scrapper verifies that the article exists and is not blocked, and that the user is not banned.
4. Saves the comment to the `Comments` table, linking it to the article and user.
5. Scrapper optionally updates the comment count for the article.
6. The response returns, and the page updates (via AJAX or full redirect).

**Forks:**
- Unauthenticated user → redirect to `/login`.
- Comment contains forbidden words (checked at Scrapper level) → 400 error, comment not saved.
- User banned by moderator → 403 error.

### 7. Moderation of Comments

**Happy path (moderator deletes a comment):**
1. Moderator (assigned by the article’s author or an admin) views the comment list for an article.
2. Clicks “Delete” next to a comment.
3. Backend sends a request to the topic `comment_delete`.
4. Scrapper verifies that the user has the `MODERATOR` role for that article (or `ADMIN` globally).
5. Deletes the comment from the database (or marks it as `DELETED`).
6. The response returns, and the comment disappears from the interface.

**Forks:**
- Moderator tries to delete a comment in an article they are not assigned to → Scrapper returns 403.
- Comment already deleted → 404 response.
- The article’s author can delete any comment in their own article (without needing the moderator role).

### 8. Assigning a Moderator

**Happy path:**
1. Article author opens moderation settings.
2. Enters a username and submits the request.
3. Backend sends a message to the topic `moderator_assign`.
4. Scrapper verifies that the specified user exists and is not already a moderator for this article.
5. Adds a record to the `Article_Moderators` table (many‑to‑many relationship).
6. The assigned user receives a notification (in‑app or by email).

**Forks:**
- User not found → 404 error.
- User is already a moderator → 409 Conflict.
- Author attempts to assign a moderator to someone else’s article → 403.

### 9. Searching for Articles (without AI)

**Happy path:**
1. User enters a search query in the search bar (e.g., “Java Spring”).
2. Backend sends the request to Scrapper, which forwards it to **ElasticSearch** (or uses PostgreSQL full‑text search if ElasticSearch is not configured).
3. ElasticSearch returns a list of article IDs sorted by relevance.
4. Scrapper loads the full article data from PostgreSQL and returns a DTO.
5. Backend displays the search results page.

*Note: ElasticSearch is not the primary search solution for articles in the system, but it can be integrated to support fallbacks when semantic search is unavailable.*

**Forks:**
- ElasticSearch unavailable → Scrapper falls back to `LIKE` queries on title and tags (performance degrades, but functionality remains).
- Empty query → returns the latest popular articles.
- No results → empty page with a suggestion to modify the query.

### 10. Profile Management and Statistics

**Happy path (profile editing):**
1. User visits `/profile`.
2. Changes name, password, avatar (image).
3. Backend sends a request to the topic `profile_update`.
4. Scrapper updates the fields in the `Users` table (password is re‑hashed).
5. When changing the avatar, the image is saved to the `Images` table (BLOB) or a separate file‑based database (Microsoft SQL Server FileTable).
6. The response returns, and the profile is updated.

**Forks:**
- Non‑unique username → 409 error.
- Invalid image format → 400 error.

### 11. Logout

**Happy path:**
1. User clicks “Logout”.
2. Backend deletes the JWT cookie (sets `Max‑Age=0`).
3. User is redirected to the main page as an unauthenticated visitor.

**Forks:** none (the operation is idempotent).

---

## Backend ↔ Scrapper Interaction via Kafka (Details)

### Topics Used for Basic Functionality (examples)

- `get_info` – requests for data (list of articles, single article, comments).
- `post_article` – creating/updating an article.
- `delete_article` – deletion.
- `post_comment` – adding a comment.
- `delete_comment` – deletion.
- `user_registration`, `user_confirm`, `user_login`, `profile_update`.
- `moderator_assign`, `moderator_remove`.

### Message Format

- Each message contains a `correlationId` (UUID) and a request object (DTO) in JSON.
- The response message contains the same `correlationId` and a response object.

### Error Handling at Kafka Level

- If Scrapper crashes, messages remain in the topic (thanks to `auto.offset.reset` and manual offset commits). After restart, Scrapper will process them.
- Topics are configured with a dead‑letter queue (DLQ) – messages that cannot be processed after several attempts are sent to a `..._dlq` topic for manual analysis.
- Backend sets a timeout on the `CompletableFuture` (e.g., 30 seconds). When the timeout expires, the user receives a 504 Gateway Timeout error.

---

## Media File Transfer (Images)

### Implementation Details

- Images **do not pass through Kafka** – this reduces broker load.
- Backend provides an endpoint `/images/{id}`.
- On an image request, Backend first checks its internal cache (e.g., Caffeine). If present, it returns immediately.
- If not, it sends an HTTP request to Scrapper, which reads the BLOB from PostgreSQL (table `Images`) and streams the bytes.
- Backend caches the retrieved image (with a configurable TTL) and serves it to the client.
- The client (browser) also caches the image according to HTTP headers.

### Forks

- Image not found → Scrapper returns 404, Backend proxies the error.
- Scrapper unavailable → Backend returns a placeholder image.

---

## Security

- **JWT** – signed, contains `sub` (username), `role`, `exp`. Stored in an `HttpOnly` cookie to protect against XSS.
- **AuthenticationFilter** (Spring Security) intercepts every request, extracts the JWT, verifies the signature and expiration, and sets the `SecurityContext`.
- **Roles:** `NOT_CONFIRMED`, `USER`, `MODERATOR`, `ADMIN`. Access to endpoints is restricted via `@PreAuthorize` or `HttpSecurity` configuration.
- **Passwords** are stored in the database as BCrypt hashes.
- **SQL injection protection** – through JPA/Hibernate parameterised queries.
- **HTTPS** – enabled on production environments (HTTP may be used during development).

---

## Testing and CI/CD

### Testing Levels

- **Unit tests** (JUnit + Mockito) – verifying the logic of controllers, services, repositories in isolation.
- **Integration tests** (Testcontainers) – spinning up containers for PostgreSQL, Kafka, Zookeeper. Checks:
   - sending and receiving messages via Kafka;
   - Liquibase operation;
   - Backend ↔ Scrapper interaction (with a real bus).
- **Checkstyle** – code style validation.
- **JaCoCo** – code coverage measurement (minimum 30% for new changes).

### CI/CD Pipeline (GitHub Actions)

- **Trigger:** pull request to the `main` branch (or push).
- **Jobs** run in parallel:
   - `build` – Maven build, run unit tests.
   - `checkstyle` – static analysis.
   - `integration-test` – run integration tests with Testcontainers.
- On success, Docker images for Backend and Scrapper are created (using the `jib‑maven‑plugin`) and published to a private registry.
- Deployment to staging/production is a separate workflow (manual or automatic).

---

## General Fault Tolerance Mechanisms

- **Kafka unavailability** – Backend cannot send a message; user receives 503. Upon recovery – retry (client‑side or producer retries).
- **Scrapper failure** – messages remain in Kafka; after restart, they will be processed.
- **Duplicate responses** – Backend deduplicates responses by `correlationId` (if the same response arrives twice, it is not processed again).
- **Task queue overflow** – Kafka allows scaling the number of partitions and consumers.
- **Caching in Backend** – reduces load on Scrapper for repeated requests (e.g., the main page article list is cached for 1 minute).