# Reading & Learning Companion

A Spring Boot 3 service that turns raw text into either a summary or a quiz. Requests are routed through an LLM-powered orchestrator that decides whether to summarise the text or generate quiz questions, and the response is secured behind JWT authentication.

## Features
- **Learning API**: Submit an instruction plus text and receive either a summary or quiz payload.
- **LLM orchestration**: Uses orchestrator and child agents (summary or quiz) with dedicated prompts.
- **Credential vault**: Store per-user API credentials (e.g., LLM keys) encrypted at rest.
- **Authentication**: Register/login endpoints issuing JWTs; `/api/**` routes require a valid token.
- **MongoDB persistence**: User accounts, credentials, and other data are stored in MongoDB.
- **Test coverage**: Unit, integration, and Testcontainers-based tests for controllers, services, JWT logic, and Mongo access.

## Architecture overview
The system follows the flow described in [`docs/architecture.md`](docs/architecture.md):
1. `LearningController` exposes `POST /api/learning`.
2. `LearningService` validates input (instruction + text) and delegates to the orchestrator.
3. `OrchestratorService` calls `QuizSummaryDecisionService` to choose SUMMARY vs QUIZ, then dispatches to `SummaryAgentService` or `QuizAgentService`.
4. Agent outputs are mapped to `LearningResponse`, containing either a summary string or a list of `QuizQuestionDto` objects.
5. LLM interactions are abstracted by `LlmClientService` (currently throws `UnsupportedOperationException` until wired to a real provider).

Prompts for the orchestrator, summary agent, and quiz agent live in `src/main/resources/prompts/`.

## API
All routes under `/api/**` require a bearer token from the auth endpoints.

### Auth
- `POST /auth/register` — body: `{ "email": "user@example.com", "password": "StrongPass!" }` → returns `{ token }`.
- `POST /auth/login` — body: `{ "email": "user@example.com", "password": "StrongPass!" }` → returns `{ token }`.

### Learning
- `POST /api/learning` — body: `{ "instruction": "SUMMARY|QUIZ|...", "text": "..." }` → returns a `LearningResponse` with the chosen action, summary, quiz questions, and explanation metadata.

### Credentials
- `GET /api/credentials` — list credentials for the current user.
- `POST /api/credentials` — create a credential `{ "provider": "OPENAI", "apiKey": "sk-..." }`.
- `DELETE /api/credentials/{id}` — remove a stored credential.

## Configuration
Key settings (see [`src/main/resources/application.yml`](src/main/resources/application.yml)):
- `security.jwt.secret` — base64 secret for signing tokens. Override via environment variables for production.
- `security.jwt.expiration-seconds` — token lifetime (default 1 day).
- `app.encryption.key-base64` — 32-byte AES key (base64). Override with `APP_ENCRYPTION_KEY_BASE64` for encryption of stored credentials.

MongoDB connection properties can be provided via standard Spring Boot properties (e.g., `SPRING_DATA_MONGODB_URI`). Tests use `spring.data.mongodb.uri=mongodb://localhost:27017/reading_test` by default.

## Running locally
Prerequisites: Java 17 and access to MongoDB (local or container).

1. Start MongoDB (example):
   ```bash
   docker run -d --name reading-mongo -p 27017:27017 mongo:7
   ```
2. Run the application:
   ```bash
   ./gradlew bootRun
   ```
3. Call auth endpoints to obtain a JWT, then include `Authorization: Bearer <token>` when hitting `/api/learning` or credential routes.

## Testing
Some tests rely on Docker (Testcontainers) and MongoDB. Ensure Docker is available and MongoDB is reachable for the `local-mongo` profile tests.

Run the full suite:
```bash
./gradlew test
```

## Notes
- The LLM client is intentionally unimplemented; wire `LlmClientService.chat` to your provider of choice and update `LlmClientConfig` with API credentials.
- Default secrets in `application.yml` are for development only; set real values in production.
