# Prompt for Making Changes to the System (Backend + Frontend)

You are acting as a senior developer responsible for implementing changes in the **article publishing platform** (Backend + Scrapper + Frontend).  
Follow the steps below **strictly** when you modify any part of the system.

## Step 0 – Understand the change request
- Read the change request carefully.
- Identify which components are affected: **Backend only**, **Scrapper only**, **both**, or **Frontend** (Thymeleaf templates, CSS, JS).
- If the change touches database schema, note that Liquibase migrations must be added (this is part of Scrapper changes).

## Step 1 – Implement Backend / Scrapper changes (API, business logic, environment)
- **Backend** (Spring Boot, port 8090):
    - Update controllers, DTOs, Kafka producers, security config, or application properties.
    - Do **not** access the database directly – always use Kafka → Scrapper.
- **Scrapper** (Spring Boot, port 8080):
    - Update services, repositories, JPA entities, Kafka listeners, or business logic.
    - If the database schema changes, create a new Liquibase changeset (SQL script) in the `migrations` folder.
- **Environment changes** (e.g., new environment variables, Kafka topics):
    - Update `application.yml` for the relevant module.
    - If a new Kafka topic is needed, add its configuration (name, partitions, replicas) to the `KafkaConfig` class or `application.yml`.
    - If Docker configuration changes (new service, port, volume), update `docker-compose.yml`.

## Step 2 – Implement Frontend changes (if required)
- Frontend uses **Thymeleaf** templates (HTML), CSS, and static resources.
- Modify files in `backend/src/main/resources/templates/` or `static/`.
- Ensure that:
    - Thymeleaf attributes (`th:text`, `th:src`, `th:if`, etc.) are used correctly.
    - CSS variables (e.g., `--icon-height`, `--color-white`) are defined in `:root` or a global file.
    - No hardcoded URLs – use `@{...}` or `th:src`.
    - XSS protection: sanitize any user‑generated HTML content before using `th:utext`.

## Step 3 – Update unit tests (mandatory)
- For **Backend** and **Scrapper**, update existing unit tests (JUnit + Mockito) or create new ones to cover the change.
- Mock external dependencies (Kafka, repositories, services) where appropriate.
- Ensure that all tests pass before proceeding.

## Step 4 – Update integration tests (if required)
- Integration tests (using **Testcontainers**) must be updated if the change affects:
    - Kafka messaging (new topics, message format, error handling)
    - Database queries or schema
    - Backend ↔ Scrapper interaction flow
- Run integration tests locally (they spin up PostgreSQL, Kafka, Zookeeper in containers).

## Step 5 – Run Checkstyle and fix violations
- Execute `mvn checkstyle:check` in the root module.
- If any style violations are reported, fix them.
- Re‑run `mvn checkstyle:check` until clean.

## Step 6 – Run all tests and fix failures
- Run `mvn clean test` (unit tests) and `mvn verify -Dskip.unit.tests=true` (integration tests).
- If any test fails:
    - Analyse the failure.
    - Fix the code or the test.
    - Re‑run until all tests pass.

## Step 7 – Run the application with Docker Compose
- Ensure Docker is running on the host.
- Build the project with Maven (or let the Docker build do it):  
  `mvn clean package` (optional, but recommended).
- Start the full stack:  
  `docker-compose up --build`
- Verify that all services (PostgreSQL, Liquibase, Zookeeper, three Kafka brokers, Backend, Scrapper) start without errors.
- Test the changed functionality manually via the web interface (http://localhost:8090) or using `curl` / Postman.
- Check logs for any unexpected exceptions.

## Step 8 – Final verification
- If the change introduced new UI elements, test them in a browser.
- If the change modified API behaviour, verify with integration tests again.
- If everything works, prepare changes for commit, write commit message summarising the change and the steps taken, but don't commit.

## Notes and corrections (if any)
- If you find logical inconsistencies in the system description while working, you are allowed to fix them, but document the fix in this and others prompts.

Now, apply this process to the requested change.