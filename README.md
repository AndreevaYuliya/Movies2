# Movies2

Monorepo:
- `movies2-app`: REST API (movies/directors), Liquibase, filtering, reports, upload.
- `email-service`: Kafka consumer + SMTP sender + retry + Elasticsearch.
- `email-api`: shared DTO (`EmailSendCommand`).

---

## Architecture & flow
- Movies app (Java 21) stores Movies/Directors (Postgres). On movie creation it publishes `email.send` (JSON DTO: subject/content/recipients).
- Email-service consumes `email.send`, stores status in Elasticsearch index `email_messages`, sends via SMTP, retries failed every 5 minutes.
- docker-compose brings Kafka+Zookeeper, Postgres, Elasticsearch, Kibana, MailHog (SMTP), email-service.

Ports: movies-app 8080 (local), email-service 8081 (compose), Kafka 9092/29092, Postgres 5432, ES 9200, Kibana 5601, MailHog 1025/8025.

---

## Quick start (Docker Compose: infra + email-service + MailHog)
Prereqs: Docker running.
1) Configure `email-service/.env` (SMTP_HOST/PORT/USER/PASS, ELASTIC_URI, KAFKA_BOOTSTRAP=kafka:29092). Defaults point to MailHog (`SMTP_HOST=mailhog`, `SMTP_PORT=1025`).
2) From repo root: `docker compose up --build -d`
3) Check: `docker compose ps` (kafka, zookeeper, postgres, elasticsearch, kibana, email-service, mailhog).
4) MailHog UI: http://localhost:8025 (SMTP capture).
5) Stop: `docker compose down`
Movies app is not in compose — run separately (see Local run).

---

## Local run (apps)
Prereqs: Java 21, Maven, Postgres at `localhost:5432` (db/user/pass `postgres`), Kafka at `localhost:9092`.
- movies-app: `mvn -pl movies2-app spring-boot:run`
- email-service: `mvn -pl email-service spring-boot:run` (set `KAFKA_BOOTSTRAP=localhost:9092`, `ELASTIC_URI=http://localhost:9200`, SMTP envs if not using MailHog)
API base: `http://localhost:8080/api/...`

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
- `POST /api/movies/upload` — multipart file (JSON array with `title`, `director`, `yearReleased`, `genres`)
- Directors CRUD: `/api/directors`

Upload sample:
```json
[
  {"title":"Inception","director":"Christopher Nolan","yearReleased":2010,"genres":"Sci-Fi"}
]
```

---

## Message contract (email.send)
Topic: `email.send`
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
- Fields: subject, content, recipients, status (NEW/SENT/FAILED), errorMessage, attempt, lastAttemptAt, createdAt.
- Kibana: http://localhost:5601 → create data view `email_messages*` to browse status history.

---

## Notes
- movies2-app uses shared `email-api` DTO; it does not include email-service code on its classpath.
- Compose SMTP defaults to MailHog; switch `.env` to real SMTP if needed.
