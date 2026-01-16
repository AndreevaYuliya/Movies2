# Movies2

Monorepo with:
- `movies2-app`: REST API for Movies/Directors (many-to-one) with Liquibase, filtering, reports, upload.
- `email-service`: Kafka consumer + SMTP sender + retry + Elasticsearch storage of email statuses.
- `email-api`: shared DTO (`EmailSendCommand`) for producer/consumer.

---

## Architecture & flow
- Movies app (Java 21, Spring Boot) persists Movies/Directors (Postgres, Liquibase).
- On movie creation, movies-app publishes Kafka message `email.send` (JSON `EmailSendCommand` with `subject/content/recipients`).
- Email-service (Spring Boot) consumes `email.send`, saves to Elasticsearch index `email_messages`, sends via SMTP, and retries failed every 5 minutes (@EnableScheduling). Status/error stored in ES.
- docker-compose brings infra: Kafka+Zookeeper, Postgres, Elasticsearch, Kibana, email-service.

Ports (default):
- movies-app: 8080 (run locally)
- email-service: 8081 (via compose)
- Kafka: 9092 (external), 29092 (internal)
- Postgres: 5432
- Elasticsearch: 9200
- Kibana: 5601

---

## Quick start (Docker Compose: infra + email-service)
Prereqs: Docker running.
1) Configure `email-service/.env` (SMTP_HOST/PORT/USER/PASS, ELASTIC_URI, KAFKA_BOOTSTRAP=kafka:29092).
2) From repo root: `docker compose up --build -d`
3) Check: `docker compose ps` (kafka, zookeeper, postgres, elasticsearch, kibana, email-service).
4) Stop: `docker compose down`
Movies app не в compose — запускайте отдельно (ниже).

---

## Local run (apps)
Prereqs: Java 21, Maven, Postgres at `localhost:5432` (db/user/pass `postgres`), Kafka at `localhost:9092`.
- movies-app: `mvn -pl movies2-app spring-boot:run`
- email-service: `mvn -pl email-service spring-boot:run` (set `KAFKA_BOOTSTRAP=localhost:9092`, `ELASTIC_URI=http://localhost:9200`, SMTP envs)
API: `http://localhost:8080/api/...`

---

## Tests
- All modules: `mvn test`
- Per module: `mvn -pl movies2-app test` or `mvn -pl email-service test`

---

## Key endpoints (movies2-app)
- `POST /api/movies` — create movie
- `GET /api/movies/{id}`
- `PUT /api/movies/{id}`
- `DELETE /api/movies/{id}`
- `POST /api/movies/_list` — filter + pagination
- `POST /api/movies/_report` — CSV
- `POST /api/movies/upload` — multipart file upload (JSON array with `title`, `director`, `yearReleased`, `genres`)
- Directors CRUD: `/api/directors`

Upload sample (multipart file):
```json
[
  {"title":"Inception","director":"Christopher Nolan","yearReleased":2010,"genres":"Sci-Fi"}
]
```

---

## Message contract (email.send)
Topic: `email.send`  
Payload (`com.movies2.email.EmailSendCommand`):
```json
{
  "subject": "New entity created",
  "content": "A new entity was created in Movies2",
  "recipients": ["admin@movies.com"]
}
```

---

## Elasticsearch
- Index: `email_messages`
- Stored fields: subject, content, recipients, status (NEW/SENT/FAILED), errorMessage, attempt, lastAttemptAt, createdAt.
- Kibana: `http://localhost:5601` → create data view `email_messages*` to browse status history.

---

## Notes
- movies2-app uses shared `email-api` for the Kafka DTO; it does not include email-service code on its classpath.
- email-service SMTP defaults: from `.env` (host/port/user/pass). In compose mapped to port 8081.***
