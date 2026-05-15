# CallPipeline

CallPipeline is a realtime AI voice appointment management system built with LiveKit, Spring Boot, PostgreSQL, Spring Data JPA, Hibernate, and Thymeleaf.

It works as an AI receptionist. A LiveKit voice agent speaks with callers, extracts appointment details, and sends structured JSON to a Spring Boot webhook. The backend decides whether to create, update, cancel, or fetch an appointment, then stores records in PostgreSQL.

## Features

- LiveKit Agent Builder webhook endpoint
- Voice-driven appointment operations
- CREATE_APPOINTMENT, UPDATE_APPOINTMENT, CANCEL_APPOINTMENT, FETCH_APPOINTMENT intents
- Spring Boot REST APIs
- PostgreSQL persistence with Spring Data JPA and Hibernate
- User-specific appointment access using caller phone matching
- Thymeleaf dashboard for records and status management
- Local ngrok support for LiveKit testing

## Project Structure

```text
src/main/java/com/callpipeline
  controller/   REST and dashboard controllers
  dto/          Request/response payloads
  model/        JPA entities and enums
  repository/   Spring Data repositories
  service/      Business logic and webhook processing
src/main/resources/templates/dashboard.html
src/main/resources/static/css/app.css
docs/livekit-agent-builder-prompt.md
docs/sample-create-webhook.json
```

## Requirements

- Java 21+
- Docker, optional but recommended for local PostgreSQL
- ngrok, for testing LiveKit webhooks locally

Maven is not required globally because the project includes the Maven wrapper.

## Run Locally

Start PostgreSQL:

```bash
docker compose up -d
```

Run the app:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Open the dashboard:

```text
http://localhost:8080/dashboard
```

Quick preview without PostgreSQL:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

## Environment

Copy `.env.example` into your local environment or export the variables manually.

```env
SERVER_PORT=8080
DATABASE_URL=jdbc:postgresql://localhost:5432/callpipeline
DATABASE_USERNAME=callpipeline
DATABASE_PASSWORD=callpipeline
JPA_DDL_AUTO=update
CALLPIPELINE_WEBHOOK_SECRET=
```

`CALLPIPELINE_WEBHOOK_SECRET` is optional for local demos. If you set it, send the same value in the `X-CallPipeline-Webhook-Secret` request header.

## LiveKit Webhook

Configure LiveKit Agent Builder to send call-ending structured JSON to:

```text
https://your-ngrok-url.ngrok-free.app/api/webhooks/livekit
```

Expose your local app with ngrok:

```bash
ngrok http 8080
```

Use the prompt in:

```text
docs/livekit-agent-builder-prompt.md
```

## REST APIs

Create an appointment:

```http
POST /api/appointments
```

List appointments:

```http
GET /api/appointments
GET /api/appointments?callerPhone=9876543210
```

Fetch a user-specific appointment:

```http
GET /api/appointments/{id}?callerPhone=9876543210
```

Update:

```http
PUT /api/appointments/{id}?callerPhone=9876543210
```

Cancel:

```http
POST /api/appointments/{id}/cancel?callerPhone=9876543210
```

LiveKit webhook:

```http
POST /api/webhooks/livekit
```

Sample webhook payload:

```bash
curl -X POST http://localhost:8080/api/webhooks/livekit \
  -H "Content-Type: application/json" \
  --data @docs/sample-create-webhook.json
```

## Build and Test

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Tests use an in-memory H2 database in PostgreSQL compatibility mode.
