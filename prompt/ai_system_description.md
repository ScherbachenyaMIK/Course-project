# System Overview

## Purpose

The platform provides authors with tools for creating, editing, and publishing articles, and readers with semantic search and recommendations. The implemented intelligent subsystem based on neural network technologies automates:

- generating titles and abstracts,
- improving style and readability of text,
- summarization (condensing) of articles,
- semantic search by meaning rather than keywords,
- automatic moderation of comments (detection of toxicity, spam, insults).

## Architecture (microservice, hybrid):

**Backend** – processes HTTP requests, serves the web interface (Thymeleaf), publishes events to Kafka.

**Scrapper** – contains business logic, works with the database, converts internal DTOs into requests to the AI Gateway.

**AI Gateway** – an AI task orchestrator implemented with Spring Boot. Manages request state through the `Ai_Requests` table in PostgreSQL. Uses the "database as a queue" pattern with `SELECT FOR UPDATE SKIP LOCKED`.

**PostgreSQL + pgvector** – stores relational data and vector embeddings of articles (type `vector(384)`). HNSW index for fast cosine search.

**Apache Kafka** – asynchronous bus between Backend and Scrapper (topics `get_info`, `ai_responses`).

**External AI providers** – YandexGPT, DeepSeek API, Alibaba Cloud (cloud-based). Local models – sentence-transformers for embeddings, BERT for toxicity classification.

**Python container `ai-testing-tools`** – for prompt validation, metric evaluation, load testing.

### Main Database Entities (extended):

- `Ai_Requests`: `id`, `correlation_id`, `task_type` (TEXT_GENERATION, TEXT_CLASSIFICATION, EMBEDDING_CALCULATION), `input_data` (JSONB), `result` (JSONB), `status` (NEW → WORK → DONE → SUCCESSFUL/FAILED), `user_affinity_key`, `created_at`, `updated_at`, `processed_by`.
- `Article_Embeddings`: `article_id`, `embedding` (vector(384)), `model_version`, `created_at`.

### Technologies: Spring Boot 3, Spring WebFlux (reactive client), Kafka, Docker, Testcontainers, JUnit, Mockito, Python (transformers, sentence-transformers, locust), GitHub Actions (CI/CD).

## User Roles and Main Actions

- **Author** – creates/edits an article, invokes intelligent functions: "Generate Title", "Improve Text", "Summarize Text".
- **Reader** – performs semantic search (by text query or selected article), gets similar materials.
- **Moderator** – reviews automatically labeled comments (toxicity/spam), makes final decisions.
- **System / AI Agent** – background processes: recalculating embeddings, cleaning up stale tasks (sanitizer), monitoring metrics.

## Happy Path (Main Scenarios) and Possible Forks

### 1. Generating a Title for an Article

**Happy path:**
1. Author enters article text in the web form and clicks "Generate Title".
2. Backend creates an event with `taskType = TEXT_GENERATION`, `correlationId`, `userAffinityKey` (author ID), `inputData` (article text) and publishes to Kafka topic `get_info`.
3. Scrapper consumes the event, forms a REST request to AI Gateway (synchronous call via WebClient).
4. AI Gateway immediately returns `202 Accepted` with an internal `taskId`, creates a record in the `Ai_Requests` table with status `NEW`.
5. Scheduler `TaskPollingScheduler` (period 100 ms) executes `SELECT ... FOR UPDATE SKIP LOCKED WHERE status='NEW'`, acquires the task, changes status to `WORK`, and places it into a `BlockingQueue` (Ready Pool).
6. A worker from the pool retrieves the task, based on `taskType` calls the appropriate handler (`YandexGptGenerationHandler`). The handler builds a prompt (role: "You are an editor", task: "Generate 5 title options", context: article text), calls the YandexGPT API.
7. The obtained result (list of titles) is saved in the `result` field of the record, status changes to `DONE`.
8. `ResultDispatchScheduler` periodically finds tasks with status `DONE` and sends a notification to Scrapper (HTTP callback). Scrapper publishes the response to Kafka topic `ai_responses`.
9. Backend receives the response and displays the title options to the author in the interface.

**Forks:**
- **API provider error** (timeout, 5xx, outage):  
  The handler catches the exception, task status becomes `FAILED`, error is recorded. The sanitizer later moves stuck `WORK` tasks to `FAILED`. The user sees a message: "Service temporarily unavailable, please try again later."
- **Provider switching**:  
  If a backup provider (e.g., DeepSeek API) is configured in AI Gateway, it is used on retry.
- **Long execution (>timeout)**:  
  The sanitizer scans tasks with status `WORK` longer than N seconds and moves them to `FAILED`.
- **No result after several attempts**:  
  Scrapper may return a fallback response (static title "No title") or show an error.

### 2. Improving Readability and Style of Text

**Happy path** (similar to generation, but `taskType = TEXT_IMPROVEMENT`):
1. Author selects a text fragment, clicks "Improve".
2. Backend → Kafka → Scrapper → AI Gateway (record `NEW`).
3. Handler `TextImprovementHandler` sends a prompt: "Fix grammatical errors, remove tautologies, preserve the author's style".
4. The result (improved text) is returned and displayed to the author with the option to accept changes.

**Forks:**
- **Model returns empty or too short response** – the system retries the request with a different prompt (few-shot examples).
- **Token limit exceeded** – input text is automatically truncated to the model's maximum length (with a warning to the user).
- **Request quotas** – if the author exceeds the daily limit (financial control), AI Gateway returns `429 Too Many Requests`, the interface shows a limit message.

### 3. Text Summarization (Abstract)

**Happy path:**
- Author or reader clicks "Create Short Description".  
  The process fully replicates the generation steps, but `taskType = SUMMARIZATION`.  
  Prompt: "Create an abstract of the article in 2-3 sentences, highlight key ideas."

**Forks:**
- **Request to summarize a very long article** – AI Gateway may use a model with a larger context window (e.g., via API) or split the text into parts and merge results.
- **Language not supported by the model** – handler returns an error, the interface suggests choosing another provider or performing translation.

### 4. Semantic Search for Similar Articles

**Happy path:**
1. Reader enters a text query (e.g., "how to improve memory") or selects an existing article.
2. Backend sends the request to Scrapper, which calls AI Gateway with `taskType = EMBEDDING_CALCULATION`.
3. AI Gateway computes the query embedding (via Python service sentence-transformers) and saves the result in `Ai_Requests`.
4. Scrapper receives the embedding and executes an SQL query on `Article_Embeddings`:
   ```sql
   SELECT article_id, 1 - (embedding <=> ?) as similarity
   FROM article_embeddings
   ORDER BY embedding <=> ?
   LIMIT ? OFFSET ?;
   ```
(Operator <=> – cosine distance, HNSW index accelerates the search.)

5. The list of article IDs sorted by semantic similarity is returned to Backend, which presents article cards to the reader.

**Forks:**

- **Embedding caching** – if the user repeats the same query, Backend stores the embedding in a cookie and does not call AI Gateway again.
- **Empty result** (no articles with similarity > threshold) – a message “Nothing found” is shown.
- **No embedding for a new article** – a background process (EmbeddingCalculationHandler) computes and saves it asynchronously upon publication. If a query arrives before the embedding is ready, that article is omitted (possible delay).
- **High load** – pgvector with HNSW provides sublinear complexity, but reindexing may be needed as the database grows.

### 5. Automatic Comment Toxicity Check

**Happy path (synchronous):**

1. Reader posts a comment under an article.
2. Backend publishes an event to Kafka (topic `comment_moderation`).
3. Scrapper consumes the event and synchronously calls AI Gateway with `taskType = TEXT_CLASSIFICATION`, passing the comment text.
4. AI Gateway creates a `NEW` task, and a worker (`HuggingFaceClassificationHandler`, a local BERT model) computes probabilities for classes: `toxic`, `spam`, `insult`, `neutral`.
5. The result (`score > 0.7` for toxicity) is returned to Scrapper, which:
   - if toxicity is low – the comment is published immediately;
   - if high – the comment receives the status “hidden” and is placed in a moderation queue.
6. A moderator in the panel sees the model prediction and can either confirm hiding or restore the comment.

**Happy path (asynchronous background):**

- The system periodically scans historical comments (e.g., once an hour) and re‑evaluates them, updating labels.

**Forks:**

- **Classification model unavailable** – AI Gateway switches to a backup cloud API (YandexGPT or Alibaba). If both are unavailable, the comment is sent to the moderator without an automatic label by default.
- **False positive** (neutral marked as toxic) – the moderator relabels the comment, and this record is added to the dataset for future fine‑tuning.
- **Low model confidence** (probability 0.4–0.7) – the comment is also sent to the moderator with a note “requires attention”.

## Common Forks and Fault Tolerance Mechanisms

- **Kafka unavailable** – Backend cannot send an event; the user receives an error “Service temporarily unavailable”. Upon recovery – retry.
- **AI Gateway failure** – with multiple instances running, SELECT FOR UPDATE SKIP LOCKED allows other instances to pick up tasks. The failure of one instance does not block the queue.
- **Stuck tasks** – the sanitizer moves tasks that have been in WORK state longer than the timeout to FAILED.
- **Graceful degradation** – if AI Gateway is completely unavailable, the platform disables the buttons for intelligent functions (or grays them out), but the core functionality (reading, publishing without AI) continues to work.
- **Confidentiality** – sensitive data use a local loop (deployed open‑source models); cloud APIs are called only with explicit author consent.

## Extended Scenarios

- **Recalculating embeddings when changing the model** – a background task initiated by an administrator updates the vectors for all articles.
- **Article analytics** – AI Gateway collects key topics from comments (clustering of embeddings) and provides the author with a popularity graph of topics.
- **Cross‑language translation of the abstract** – when a reader selects a different language, AI Gateway calls a translation model and caches the result.
