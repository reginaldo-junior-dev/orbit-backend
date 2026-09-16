# Orbit — Backend API

Orbit is a personal productivity SaaS concept — tasks, projects, and goals in one calm workspace. This repository is the backend: a Spring Boot REST API with JWT authentication over httpOnly cookies, CSRF protection, PostgreSQL persistence via Flyway migrations, and a real payment integration with Mercado Pago (Checkout Pro).

**API base URL:** https://orbit-api-oea3.onrender.com
**API docs (Swagger UI):** https://orbit-api-oea3.onrender.com/swagger-ui.html
**Frontend repo:** https://github.com/reginaldo-junior-dev/orbit

## Demo credentials

The deployed API has two seeded accounts, usable from the [frontend app](https://orbit-inky-xi.vercel.app):

| Role | Email | Password |
|---|---|---|
| Admin | `admin@orbit.com` | `iJpD8LqZhHz@v` |
| User | `user@orbit.com` | `ZaNGmWm5Q4@7L` |

These accounts are seeded at startup from environment variables (see below) rather than a hardcoded hash in a migration, so the real password is never committed to the repository.

## Payment testing

The API is configured with Mercado Pago **test credentials** (`MP_ACCESS_TOKEN`), so it processes payments through Mercado Pago's sandbox — no real money moves. Webhooks from test payments are signed the same way as production ones and verified with `MP_WEBHOOK_SECRET`.

> **Before testing, know this:** `MP_ACCESS_TOKEN` alone only gets you a working checkout screen. Without `MP_WEBHOOK_SECRET` set, the webhook endpoint rejects every notification with 401 — the payment will show as approved on Mercado Pago's side, but the user's plan is silently never granted, with no error surfaced anywhere. `MP_NOTIFICATION_URL` also has to be a **publicly reachable** URL (e.g. an ngrok tunnel for local dev, not `localhost`) — otherwise Mercado Pago can never reach the webhook in the first place, same silent symptom. And `FRONTEND_ORIGIN` must match the frontend's actual URL, or the post-payment redirect (success/pending/failure) lands in the wrong place.

To test the complete payment flow, use the provided test buyer account and test payment credentials:

1. Set `MP_ACCESS_TOKEN` to a **test** Access Token (see below), never a production one, in local/dev environments.
2. Run the [frontend](https://github.com/reginaldo-junior-dev/orbit) against this API and go through the checkout flow from the pricing section.
3. When Checkout Pro asks you to log in, use a Mercado Pago **test buyer account** rather than a personal one, and pay with a Mercado Pago **test card** to simulate an approved, pending, or rejected outcome.
4. On approval, Mercado Pago calls this API's webhook endpoint, which verifies the signature and grants/extends the user's plan — check the logs or the user's plan in the dashboard to confirm.

### How to access the test credentials

1. Go to the [Mercado Pago Developers panel](https://www.mercadopago.com.br/developers/panel) and log in (create a free account if you don't have one).
2. Open **Your integrations** and select the application used by this project (or create one).
3. Open **Test credentials** and copy the test **Access Token** into `MP_ACCESS_TOKEN` (and the test **Public Key**, if the frontend's SDK needs it).
4. Open **Test accounts** in the sidebar and create a **seller** test account (linked to the application) and a **buyer** test account (used to log in and pay at checkout).
5. Use the buyer account's email/password when Checkout Pro prompts for login.
6. Use a Mercado Pago test card for the payment — the card's "name on card" field (e.g. `APRO`) selects the simulated outcome (approved, rejected, pending, etc.).

> Never put a production Access Token in a local/dev environment — test and production credentials belong to separate (test vs. real) accounts on Mercado Pago's side.

## Features

- **Authentication** — register/login with BCrypt-hashed passwords, JWT issued as an httpOnly cookie, configurable `Secure`/`SameSite` for cross-domain deployments
- **CSRF protection** — double-submit cookie pattern, with a strict handler that only accepts the token from the `X-XSRF-TOKEN` header
- **Login rate limiting** — 5 failed attempts per email per 15 minutes
- **Tasks / Projects / Goals** — full CRUD, every resource scoped to its owner (a user can never read, edit, or delete another user's data)
- **Dashboard** — aggregated counts and status breakdowns across a user's tasks, projects, and goals
- **Admin** — list every user, edit their name/email/role, or delete their account (and all their data); an admin cannot edit or delete their own account through these endpoints
- **Payments (Mercado Pago Checkout Pro)** — creates a one-time payment preference for the Pro/Constellation plans, verifies the webhook signature, and grants/extends the user's plan once the payment is approved
- **API documentation** — springdoc/OpenAPI, browsable via Swagger UI
- **Global error handling** — consistent JSON error shape across validation, not-found, conflict, and auth failures

## Tech stack

- Java 21 · Spring Boot 4.1
- Spring Web, Spring Security, Spring Data JPA
- PostgreSQL + Flyway
- JWT (auth0 `java-jwt`) · BCrypt
- Mercado Pago Java SDK
- springdoc-openapi (Swagger UI)
- JUnit, Mockito, MockMvc

## Project structure

```
src/main/java/reginaldo/orbit/api/
  controller/   HTTP layer only
  service/      business logic
  repository/   Spring Data JPA repositories
  entity/       JPA entities
  dto/          request/response records, grouped by feature
  exception/    domain exceptions + global handler
  security/     JWT filter, CSRF handler, rate limiter, user details
  config/       Spring Security config, Mercado Pago init, demo account seeder
  doc/          OpenAPI annotations, kept separate from controllers
src/main/resources/db/migration/   Flyway migrations (never edit an applied one — add a new one)
```

## Getting started

Requires Java 21, Maven (or the bundled `./mvnw`), and a PostgreSQL database.

```bash
./mvnw spring-boot:run
```

### Environment variables

| Variable | Description | Required |
|---|---|---|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://host:5432/db` | Yes |
| `DB_USER` | Database user | Yes |
| `DB_PASSWORD` | Database password | Yes |
| `JWT_SECRET` | Secret used to sign JWTs | Yes |
| `FRONTEND_ORIGIN` | Allowed CORS origin (the frontend's URL) | No (default `http://localhost:5173`) |
| `COOKIE_SECURE` | `true` in production (requires HTTPS) | No (default `false`) |
| `COOKIE_SAME_SITE` | `None` when frontend and backend are on different domains, `Strict` for local dev | No (default `Strict`) |
| `MP_ACCESS_TOKEN` | Mercado Pago access token | Yes |
| `MP_WEBHOOK_SECRET` | Mercado Pago webhook signing secret, used to verify notifications | No, but webhooks are rejected without it |
| `MP_NOTIFICATION_URL` | This API's own public URL, used to build the webhook callback | No |
| `DEMO_ADMIN_EMAIL` / `DEMO_USER_EMAIL` | Override the seeded demo account emails | No (defaults shown above) |
| `DEMO_ADMIN_PASSWORD` / `DEMO_USER_PASSWORD` | Password for the seeded demo accounts | No — skipped if unset |

### Tests

```bash
./mvnw test
```

### Docker

```bash
docker build -t orbit-api .
docker run -p 10000:10000 --env-file .env orbit-api
```

### Docker Compose (API + Postgres + frontend, all at once)

`docker-compose.yml` in this repo runs everything needed to try the app locally — no local Java, Maven, Node, or Postgres install required.

It builds the frontend from a **sibling directory**, so clone [orbit](https://github.com/reginaldo-junior-dev/orbit) next to this repo first:

```
some-folder/
  orbit/           <- frontend
  orbit-api/       <- this repo
```

Then, from this repo:

```bash
cp .env.example .env   # fill in the values described below
docker compose up --build
```

- App: http://localhost:8081
- API (direct, mostly for debugging): http://localhost:8080
- Postgres (mostly for debugging, e.g. `psql`): `127.0.0.1:5432`, bound to localhost only

What goes in `.env` (see `.env.example` for the full list):

- `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` — pick any values, this is a throwaway local database
- `JWT_SECRET` — generate your own (e.g. `openssl rand -base64 48`); don't reuse this value anywhere real
- `MP_ACCESS_TOKEN` / `MP_WEBHOOK_SECRET` — Mercado Pago **test** credentials, see [Payment testing](#payment-testing) above
- `MP_NOTIFICATION_URL` — optional, only needed to test the payment webhook end-to-end (requires a public tunnel, e.g. `ngrok http 8081`); leave blank otherwise
- `DEMO_*` — optional, seeds the demo accounts on startup

`.env` is gitignored and `docker-compose.yml` has no secrets or default credentials baked in — nothing sensitive ends up in either repo's history.

## Development note

This project was developed with the assistance of Claude Code for coding suggestions, debugging, and refactoring.
