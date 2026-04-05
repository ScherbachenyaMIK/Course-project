# AI Agent Task: Migrate to Custom PostgreSQL with pgvector + FTS

## Context
We currently use a standard PostgreSQL database for storing articles. We need to add **full‑text search (FTS)** as a fallback to a future semantic search (pgvector). The solution must be a **single custom PostgreSQL image** that includes both `pgvector` and built‑in FTS with a generated `tsvector` column. The new database will replace the old one.

## Task Overview
1. Build a custom PostgreSQL Docker image with `pgvector` extension.
2. Integrate the image into the application infrastructure (docker-compose / k8s).
3. Write Liquibase migrations to add FTS columns and indexes.
4. Migrate existing data from the old database to the new schema.
5. Decommission the old database.
6. Update the application’s article saving logic to automatically maintain the `tsvector` column using `title` + `text_content`.

---

## Step 1 – Custom PostgreSQL Image

Create a `Dockerfile` based on `postgres:15` (or 16). Install `pgvector` from source, enable the extension, and optionally preconfigure `postgresql.conf`.

```dockerfile
FROM postgres:15

RUN apt-get update && apt-get install -y \
    build-essential \
    postgresql-server-dev-15 \
    git \
    && git clone https://github.com/pgvector/pgvector.git \
    && cd pgvector \
    && make \
    && make install \
    && apt-get remove -y build-essential git \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/* /pgvector

# Enable vector extension in shared_preload_libraries (optional but recommended)
RUN echo "shared_preload_libraries = 'vector'" >> /usr/share/postgresql/postgresql.conf.sample
```

Build and tag the image: `myapp/postgres-pgvector:latest`. Push to your registry.

## Step 2 – Update Infrastructure

Modify your `docker-compose.yml` (or Helm chart) to use the custom image instead of the standard `postgres`. Ensure persistent volumes remain compatible (no change needed). Example:

```yaml
services:
  postgres:
    image: myapp/postgres-pgvector:latest
    environment:
      POSTGRES_DB: myapp
      POSTGRES_USER: myapp
      POSTGRES_PASSWORD: secret
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
```

## Step 3 – Liquibase Migrations

Create a new Liquibase changelog (e.g., `v2-add-fts-and-vector.xml`) that:

- Enables the `vector` extension.
- Adds a generated `tsvector` column to the `articles` table using `title` and `text_content`.
- Creates a GIN index on that column for fast FTS.
- (Optional) Adds a `content_embedding` column of type `vector(384)` if semantic search is planned – but for now we only require FTS.

**Example migration:**

```sql
-- liquibase formatted sql

-- changeset 1: enable extensions
CREATE EXTENSION IF NOT EXISTS vector;

-- changeset 2: add tsvector column
ALTER TABLE articles
ADD COLUMN search_vector tsvector
GENERATED ALWAYS AS (
    setweight(to_tsvector('russian', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('russian', coalesce(text_content, '')), 'B')
) STORED;

-- changeset 3: create GIN index
CREATE INDEX idx_articles_search_vector ON articles USING GIN (search_vector);
```

**Notes:**
- Use `'russian'` config – change if another language is primary.
- Weight `'A'` for title, `'B'` for content – adjust as needed.
- For existing rows, the `STORED` column will be automatically populated when you run `ALTER TABLE` (PostgreSQL 12+). If not, run a manual `UPDATE articles SET search_vector = ...` once.

## Step 4 – Migrate Data from Old Database

We assume the old database is still running. Steps:

1. **Backup** the old database using `pg_dump` (or use a migration tool like `pgloader`).
2. **Restore** the dump into the new database. Since the schema is extended with new columns, the dump should only contain data for existing columns. Use `--data-only` or `--inserts` to avoid conflicts.
    - Safer approach: Use `pg_dump --data-only --table=articles old_db | psql new_db`
3. After restore, the generated `search_vector` column will be empty for old rows. Force recomputation:
   ```sql
   UPDATE articles SET search_vector = 
       setweight(to_tsvector('russian', coalesce(title, '')), 'A') ||
       setweight(to_tsvector('russian', coalesce(text_content, '')), 'B');
   ```
   (This can be done inside a Liquibase migration after data load.)

Alternatively, use a one‑time migration script (Python, Go, etc.) that reads from old DB and writes to new DB, but SQL‑level is simpler.

## Step 5 – Decommission the Old Database

After verifying that:
- All data is present in the new database.
- FTS queries return expected results.
- Application works correctly with the new database.

Then:
- Stop the old database container/service.
- Remove its volumes (after a final backup).
- Update any connection strings or secrets to point only to the new database.
- Remove old database entries from orchestration (docker-compose, k8s deployment).

## Step 6 – Update Application Logic for Saving Articles

Modify the code that inserts/updates articles (e.g., repository layer). Because the `search_vector` column is **generated**, you do **not** need to compute or set it manually. The database will automatically update it when `title` or `text_content` changes.

**Before:**
```python
def save_article(article):
    cursor.execute(
        "INSERT INTO articles (title, text_content) VALUES (%s, %s)",
        (article.title, article.text_content)
    )
```

**After:** No change needed – the generated column works transparently. However, if you ever need to query using FTS, implement a fallback method:

```python
def search_articles(query: str, use_vector_search=True):
    if use_vector_search:
        try:
            # Vector similarity search (requires embedding column)
            return vector_search(query)
        except Exception as e:
            logger.warning(f"Vector search failed, falling back to FTS: {e}")
    # Fallback to full‑text search
    return db.execute("""
        SELECT id, title, text_content,
               ts_rank(search_vector, plainto_tsquery('russian', %s)) as rank
        FROM articles
        WHERE search_vector @@ plainto_tsquery('russian', %s)
        ORDER BY rank DESC
        LIMIT 20
    """, (query, query))
```

**Important:** The `search_vector` column is now automatically maintained for **new** and **updated** articles. No extra application code is required for maintenance.

---

## Additional Considerations

- **Language**: Use the correct text search configuration (e.g., `'russian'`, `'english'`, or `'simple'`). If your content is multilingual, consider storing the language per row. - **Our primary language is Russian with some English words**
- **Index maintenance**: GIN index updates automatically on insert/update – no extra work.
- **Performance**: For tables >1M rows, monitor vacuum and analyze. GIN index may become large; you can later switch to `gin_pending_list_limit` tuning.
- **Fallback scenario**: The FTS is a fallback *inside the same database*. If the whole database is down, both vector and FTS fail. That’s acceptable for your use case.

---

## Verification Checklist

- [ ] Docker image builds without errors.
- [ ] Container starts and `CREATE EXTENSION vector` succeeds.
- [ ] Liquibase migrations apply cleanly on an empty database.
- [ ] After data migration, `search_vector` is not null for all rows.
- [ ] FTS query returns relevant results (test with `plainto_tsquery`).
- [ ] Application save logic does not break (generated column ignored on insert).
- [ ] Old database is safely removed after validation.

---

## Expected Deliverables

- Dockerfile and build script.
- Updated docker-compose / deployment manifests.
- Liquibase migration scripts (SQL or XML).
- Data migration plan (e.g., one‑time script or SQL commands).
- Pull request with application code changes (if any – minimal).

---

**Note to AI agent:** Execute these steps sequentially. If any step fails, provide a clear error message and a rollback plan. Assume the current database is named `scrapper` and the table is `articles` with columns `id`, `title`, `text_content`. Adapt column names as needed.