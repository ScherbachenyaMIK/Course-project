# Negative Prompt: What NOT to Do When Implementing the Happy Path for Viewing an Article

1. **Do not access the database directly from the Backend module**  
   *Forbidden:* Using `@Autowired` JPA repository or JDBC template inside `ArticlesController` or any other component of the backend module.

   *Why:* In the architecture, only the Scrapper module has direct access to PostgreSQL. Backend must always send a request via Kafka and wait for a response. Direct access violates microservice separation of responsibilities and creates tight coupling.

2. **Do not synchronously call Scrapper via HTTP/REST instead of Kafka**  
   *Forbidden:* Using `RestTemplate` or `WebClient` to synchronously call Scrapper from the controller, blocking the thread until the response is received.

   *Why:* This kills asynchronicity, increases response time (especially during failures), reduces fault tolerance and scalability. All requests between Backend and Scrapper must go through Kafka.

3. **Do not block the controller thread (e.g., by calling `future.get()`)**  
   *Forbidden:* In the controller method, calling `future.get()` or `future.join()` for synchronous waiting.

   *Why:* Spring MVC expects the method to return `CompletableFuture` or `Callable` to release the container thread. Blocking leads to thread pool exhaustion and performance degradation.

4. **Do not ignore error handling and timeouts**  
   *Forbidden:* Sending a request to Kafka without setting a timeout on the `CompletableFuture` and without handling `KafkaException`, `TimeoutException`.

   *Why:* The user will hang indefinitely or receive a non‑informative error. Always complete the future with an error and return a meaningful HTTP status (503, 504, 500).

5. **Do not store user session on the server (`HttpSession`)**  
   *Forbidden:* Using `HttpSession` to store authentication state or `CompletableFuture` maps.

   *Why:* Session does not scale horizontally (without sticky sessions). The project uses JWT in cookies – it is stateless. The `pendingResponses` map is an exception, but it must not be tied to a session; only to `correlationId`.

6. **Do not return HTML content without escaping (XSS)**  
   *Forbidden:* Using `th:utext` without prior sanitization for the user‑supplied HTML content of the article.

   *Why:* A user could insert `<script>alert('XSS')</script>` into an article. Thymeleaf escapes by default, but `utext` disables escaping. Either apply a sanitization library (OWASP Java HTML Sanitizer) or allow only safe tags.

7. **Do not reveal the existence of a hidden article to unauthorised users**  
   *Forbidden:* Returning status 403 or 200 with empty content for a hidden article when the user is not the author/admin.

   *Why:* An attacker could brute‑force IDs and determine the presence of hidden articles. Return 404 (as if the article does not exist).

8. **Do not pass unhandled exceptions from the service to the template**  
   *Forbidden:* Allowing an exception from `articlesService` to reach the controller without catching and logging.

   *Why:* The user will see a stack trace (if `@ControllerAdvice` is not configured) or get a 500 without any diagnostic information. Catch, log, and return a user‑friendly error page.

9. **Do not ignore deduplication of responses by `correlationId`**  
   *Forbidden:* When receiving a response from Kafka, not checking whether the `CompletableFuture` is already completed, or completing it again.

   *Why:* Kafka may deliver a message twice (at‑least‑once semantics). Without a check, `future.complete(...)` may throw an exception, and the user could receive an error.

10. **Do not hardcode topic names in the code**  
    *Forbidden:* Writing `kafkaTemplate.send("get_info", ...)` with string literals scattered across classes.

    *Why:* Hardcoding makes maintenance and configuration changes difficult. Move topic names to `application.yml` and use `@Value` or `@ConfigurationProperties`.

11. **Do not load images in the same synchronous manner as text**  
    *Forbidden:* Passing image BLOBs via Kafka together with the article DTO.

    *Why:* Kafka is not designed for large binary data; it can lead to memory overflow and broker crashes. Images must be transferred separately via HTTP (as described in the architecture).

12. **Do not cache responses from Scrapper indefinitely**  
    *Forbidden:* Storing `CompletableFuture` in a static map without removing it after completion.

    *Why:* Memory leak. Remove the entry from `pendingResponses` after `complete` or `completeExceptionally`.

13. **Do not use `@EnableAsync` and `@Async` to mimic asynchronicity**  
    *Forbidden:* Trying to make the controller asynchronous via `@Async` and returning `Future`.

    *Why:* This does not solve the problem of interacting with Kafka; it only adds unnecessary threads. The correct way is to return a `CompletableFuture` that will be completed by a Kafka listener.

14. **Do not use `!important` in CSS unless absolutely necessary**  
    *Forbidden:* Overusing `!important` to override styles.

    *Why:* It complicates maintenance and cascade inheritance. In the provided CSS, `!important` is absent – that is a good practice.

15. **Do not ignore permission checks before displaying edit/moderation buttons**  
    *Forbidden:* Showing an “Edit” button on the article page to all users if they are not the author.

    *Why:* Security through obscurity is not sufficient. Buttons should be rendered conditionally using `th:if` based on the role extracted from the JWT.

16. **Do not parse JSON manually (without DTOs)**  
    *Forbidden:* Using `JsonNode` or `ObjectMapper.readValue` for each message manually.

    *Why:* It increases boilerplate code and the risk of errors. Use typed DTOs and deserialization via Spring Kafka (`JsonDeserializer`).