# Happy Path – Viewing and Reading an Article

**Purpose:** Implement the full positive scenario of displaying an article to the user. All components (Backend, Kafka, Scrapper, DB) are working normally, the article exists, is published, and is visible.

**Expected behavior:**

- User navigates to the URL `/articles/{id}`.
- The Backend controller generates a `correlationId`, sends a request to Kafka (topic `get_info`) with an `ArticleRequest` object.
- Scrapper receives the request, calls `articlesService.getArticle(id)`.
- If the article exists and the `visibility` field is `true` (published), the handler converts it to an `ArticleDTO` and sends a response to Kafka (topic `get_info_response` with the same `correlationId`).
- Backend receives the response, `ResponseHandler` completes the `CompletableFuture`, returns a `ModelAndView` with the template name (e.g., `"article"`) and the `article` object.
- Thymeleaf renders the page, inserting data: title, author, avatar, update date, tags, categories, reading time, creation date, status, HTML content, view/like/comment counters.
- The user sees a fully styled article with the correct styles (CSS from `article.css`).

**Implementation requirements (what Claude Code should generate/verify):**

- **DTOs** (`ArticleRequest`, `ArticleDTO`, `ArticleInformationDTO`) – fields match the template.
- **Repository** – method `findArticleById(Long id)` returns `Optional<Article>`.
- **Service** – method `getArticle(id)` returns `null` if the article is not found or hidden (visibility logic can be placed here, but the handler already checks `visibility`).
- **Handler** (`handleArticleRequest`) – must handle only visible articles. If the article is not found or `visibility == false`, return `ArticleDTO.emptyDTO()` (or a special object with an `isEmpty` flag).
- **Kafka configuration** – topics `get_info` and `get_info_response` with the necessary partitions/replicas.
- **ResponseHandler** – method `getResponse(correlationId, isAuthenticated)` that stores a `CompletableFuture` in a map and returns it. When a response from Kafka arrives with that `correlationId`, it completes the future.
- **ScrapperProducer** – method `sendGetRequest(topic, correlationId, request)` serialises the request to JSON and sends it.
- **Controller** – already exists, but ensure it handles the response from `ResponseHandler` and does not block the thread.
- **HTML template** – uses `th:src`, `th:text`, `th:utext` attributes to insert data. Must correctly handle empty values (e.g., if no tags, display "Tags: none").
- **CSS** – must be applied; variables (`--icon-height`, `--color-white`, etc.) must be defined in a global file or in `:root`.

**Success criteria (for Claude Code to verify):**

- After the request, Backend logs show a message being sent to Kafka.
- Scrapper logs show the message reception and a call to `handleArticleRequest`.
- The database contains a test article with `visibility = true`.
- The page renders without 4xx/5xx errors, all data is substituted correctly.
- The author's avatar (image) loads via the `/images/{id}` endpoint (if implemented).

---

# Negative Scenarios (Errors and Edge Cases)

**Purpose:** Handle all possible failures and incorrect situations when viewing an article. The code must be resilient and return clear error messages to the user.

## List of Negative Scenarios and Expected System Actions

### 1. Article Not Found (ID does not exist in the DB)
- **Action:** Scrapper receives `null` from the service.
- **Reaction:** Handler returns `ArticleDTO.emptyDTO()` (or a special DTO with a `notFound = true` flag).
- **Backend:** Recognises the empty DTO, returns a `ModelAndView` with the template `"error/404"` and HTTP status 404.
- **User sees:** A “Article not found” page with a suggestion to go to the main page.

### 2. Article Exists but Is Hidden (`visibility = false`)
- **User:** Anyone (even the author).
- **Reaction:** Same as scenario 1 – return 404. **Important:** Do not reveal the existence of a hidden article to unauthorised users.
- **Exception:** If the current user is the **author of the article** or an **administrator**, the hidden article should be visible (with a note “Draft” or “Hidden”).
   - For this, `ArticleRequest` should contain the `userId` of the current user (from `AuthenticationChecker.checkAuthorities()` or the JWT). The handler checks: if `article.getAuthorId().equals(userId)` or role `ADMIN` – ignore `visibility`.

### 3. Kafka Unavailable When Sending the Request
- **Action:** `ScrapperProducer` fails to send the message (exception `TimeoutException`, `KafkaException`).
- **Reaction:** The controller must catch the exception and complete the `CompletableFuture` with an error. A global error handler (`@ControllerAdvice`) returns a 503 Service Unavailable page with the text “Service temporarily unavailable, please try again later”.

### 4. Timeout Waiting for a Response from Scrapper
- **Action:** `ResponseHandler` sets a timeout on the `CompletableFuture` (e.g., 30 seconds). If no response arrives, the future completes with a timeout.
- **Reaction:** Return 504 Gateway Timeout with the message “The server did not respond in time”.

### 5. Scrapper Returns an Invalid DTO (e.g., missing required fields)
- **Action:** JSON deserialisation into `ArticleDTO` fails.
- **Reaction:** The Kafka listener in Backend catches a `DeserializationException`, logs the error, and sends the message to a DLQ (dead letter queue). The user receives a 500 Internal Server Error.

### 6. Database Error (connection timeout, SQLException)
- **Action:** `articlesService.getArticle(id)` throws an exception.
- **Reaction:** The handler catches the exception and returns a DTO with an `error = true` flag. Backend translates it to a 500.

### 7. Attempt to Access an Article That Requires Authentication (if such functionality exists in the future)
- **Currently not required**, but for future: if `article.requiresAuth == true`, check `isAuthenticated` from `AuthenticationChecker`. If not authenticated, redirect to `/login`.

### 8. Invalid ID Format (e.g., letters instead of numbers)
- **Action:** Spring MVC automatically returns 400 Bad Request because `@PathVariable Long id` cannot be converted. This is expected.

### 9. Article Has Very Long Content (several megabytes)
- **Reaction:** Scrapper should return the content as‑is, without truncation. Backend should stream it (but Thymeleaf will generate the whole page, which could be problematic). Future improvements could include pagination within the article or virtual scrolling. At this stage, limit content size at the DB level (TEXT up to 1 MB) and warn the author during saving.

### 10. Author’s Avatar Not Uploaded or Corrupted
- **Reaction:** Instead of `article.authorIconUri`, substitute a placeholder URI (e.g., `/resources/default-avatar.png`). This should be implemented in the service or the template via `th:src` with a condition.

---

## Implementation Requirements

- Add a `currentUserId` field to `ArticleRequest` (optional) for checking permissions on hidden articles.
- Implement the following logic in the handler:
  ```java
  if (article == null) return emptyDTO();
  if (!article.getVisibility() && !isAuthorOrAdmin(article, userId)) return emptyDTO();
  ```
- Add a timeout in `ResponseHandler`:
  ```java
  CompletableFuture<ModelAndView> future = new CompletableFuture<>();
  pendingResponses.put(correlationId, future);
  ScheduledExecutorService.schedule(() -> {
      future.completeExceptionally(new TimeoutException());
      pendingResponses.remove(correlationId);
  }, timeoutSeconds, TimeUnit.SECONDS);
  ```
- Create a global error handler (`@ControllerAdvice`) with methods:
  - handleKafkaException → 503
  - handleTimeoutException → 504
  - handleArticleNotFound → 404 (when DTO is empty)
- Logging must include clear error messages with correlationId for tracing.

## Success Criteria (Verifying Negative Scenarios)

- When requesting a non‑existent ID → a 404 page, no exceptions in Scrapper logs.
- When Kafka is stopped → a 503 page with a service unavailable message.
- On timeout → a 504 page.
- The author sees their own hidden article; another user sees a 404.