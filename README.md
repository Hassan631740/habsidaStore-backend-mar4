# habsidaStore Backend

Spring Boot 4 / Java 21 / PostgreSQL multi-role store backend.

## Prerequisites

| Tool | Minimum version |
|------|----------------|
| Java | 21 |
| Maven | 3.9+ (or use `./mvnw`) |
| Docker & Docker Compose | v2 |
| PostgreSQL | 15+ (only if running outside Docker) |

---

## Quick start

### Option A — Docker (recommended, zero local DB required)

```bash
cp .env.example .env          # set JWT_SECRET and leave the rest as-is
docker compose up -d          # starts postgres + app
```

- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

Rebuild after code changes:

```bash
docker compose up -d --build
```

### Option B — Local (IDE or terminal)

1. Start PostgreSQL locally (or via Docker for only the DB):

   ```bash
   docker run -d --name pg \
     -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
     -e POSTGRES_DB=habsida_store \
     -p 5432:5432 postgres:16-alpine
   ```

2. Copy and edit the env file:

   ```bash
   cp .env.example .env
   ```

3. Run the app with the `local` profile (includes seed data):

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

- App: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

---

## Environment variables

All variables are read from `.env` (or system environment). The `local` profile provides safe defaults so only `JWT_SECRET` is strictly required when running locally.

| Variable | Required in prod | Default (local) | Description |
|----------|-----------------|-----------------|-------------|
| `SPRING_PROFILES_ACTIVE` | yes | `local` | Active Spring profile |
| `DATABASE_URL` | yes | `jdbc:postgresql://localhost:5432/habsida_store` | JDBC URL |
| `DATABASE_USERNAME` | yes | `postgres` | DB user |
| `DATABASE_PASSWORD` | yes | `postgres` | DB password |
| `JWT_SECRET` | yes | `local-dev-secret-at-least-32-characters-long` | HS256 signing key (min 32 chars) |
| `JWT_EXPIRATION_MS` | no | `86400000` (24 h) | Access token TTL |
| `JWT_REFRESH_EXPIRATION_MS` | no | `604800000` (7 d) | Refresh token TTL |
| `SERVER_PORT` | no | `8080` | HTTP port |
| `CORS_ALLOWED_ORIGINS` | no | `http://localhost:3000` | Comma-separated allowed origins |

---

## Seed data (local / dev profiles)

When the `local` or `dev` profile is active, Flyway also runs scripts from `classpath:db/seed`, which inserts demo data. The accounts below are available immediately after first startup.

| Role | Email | Password | Notes |
|------|-------|----------|-------|
| ADMIN | admin@habsida.com | `admin123` | Full system access |
| MERCHANT | merchant1@habsida.com | `merchant123` | Owns "Coffee House" (store 1) |
| MERCHANT | merchant2@habsida.com | `merchant123` | Owns "Tea Garden" (store 2) |
| CUSTOMER | customer@habsida.com | `customer123` | Has one sample order |

To obtain a JWT, POST to `/api/auth/login`:

```json
{ "email": "admin@habsida.com", "password": "admin123" }
```

---

## API documentation (Swagger)

| URL | Description |
|-----|-------------|
| http://localhost:8080/swagger-ui/index.html | Interactive Swagger UI |
| http://localhost:8080/v3/api-docs | OpenAPI 3 JSON spec |

All endpoints are grouped by tag:

| Tag | Path prefix | Auth |
|-----|-------------|------|
| Catalog | `/api/catalog/**` | None |
| Customer Orders | `/api/me/orders/**` | CUSTOMER |
| Customer Addresses | `/api/me/addresses/**` | CUSTOMER |
| Merchant Orders | `/api/merchant/orders/**` | MERCHANT |
| Merchant Customers | `/api/merchant/customers/**` | MERCHANT |
| Stores | `/api/merchant/stores/**`, `/api/admin/stores/**` | MERCHANT / ADMIN |
| Products | `/api/merchant/products/**`, `/api/admin/stores/{id}/products/**` | MERCHANT / ADMIN |
| Categories | `/api/merchant/categories/**`, `/api/admin/stores/{id}/categories/**` | MERCHANT / ADMIN |
| Modifiers | `/api/merchant/modifier-groups/**`, `/api/admin/stores/{id}/modifier-groups/**` | MERCHANT / ADMIN |
| Store Settings (Merchant) | `/api/merchant/settings/**` | MERCHANT |
| Store Settings (Admin) | `/api/admin/stores/{id}/settings/**` | ADMIN |
| Admin Orders | `/api/admin/orders/**` | ADMIN |
| Admin Customers | `/api/admin/customers/**` | ADMIN |
| Admin Users | `/api/admin/users/**` | ADMIN |

---

## Running tests

```bash
# All tests (unit + integration — requires Docker for Testcontainers)
./mvnw test

# Unit tests only (no Docker needed)
./mvnw test -pl . -Dtest="OrderLifecycleServiceTest,OrderPlacementServiceTest"

# Integration / security tests (Testcontainers spins up Postgres automatically)
./mvnw test -Dtest="OrderPlacementIntegrationTest,MerchantIsolationTest"
```

Test coverage:
- **Unit tests** — `OrderLifecycleServiceTest` (17 cases: status machine transitions, access control) and `OrderPlacementServiceTest` (9 cases: totals, modifiers, validation)
- **Integration tests** — `OrderPlacementIntegrationTest` (3 cases: DB persistence, totals, snapshots against real Postgres via Testcontainers)
- **Security tests** — `MerchantIsolationTest` (5 cases: cross-store data isolation)

---

## Project structure

```
src/
├── main/
│   ├── java/com/habsida/store/
│   │   ├── controller/          # REST controllers by role
│   │   │   ├── admin/           # /api/admin/**  (ROLE_ADMIN)
│   │   │   ├── merchant/        # /api/merchant/** (ROLE_MERCHANT)
│   │   │   ├── catalog/         # /api/catalog/** (public)
│   │   │   ├── me/              # /api/me/**      (ROLE_CUSTOMER)
│   │   │   └── OrderController  # /api/me/orders  (ROLE_CUSTOMER)
│   │   ├── service/             # Business logic
│   │   ├── entity/              # JPA entities
│   │   ├── repository/          # Spring Data JPA repositories
│   │   ├── dto/                 # Request/response DTOs and DtoMapper
│   │   ├── security/            # JWT filter, AuthUser, SecurityConfig
│   │   ├── spec/                # JPA Specifications for filtering
│   │   └── exception/           # GlobalExceptionHandler, custom exceptions
│   └── resources/
│       ├── application.properties          # Base config
│       ├── application-local.properties    # Local profile (with seed)
│       ├── application-dev.properties      # Dev profile (with seed)
│       ├── application-prod.properties     # Production profile
│       └── db/
│           ├── migration/   # Flyway V1–V15 schema migrations
│           └── seed/        # V100 demo data (local/dev only)
└── test/
    └── java/com/habsida/store/
        ├── service/             # Unit tests (Mockito)
        └── integration/         # Integration tests (Testcontainers)
```

---

## Spring profiles

| Profile | Flyway locations | SQL logging | Swagger | Seed data |
|---------|-----------------|-------------|---------|-----------|
| `local` | migration + seed | yes | yes | yes |
| `dev` | migration + seed | yes | yes | yes |
| `prod` | migration only | no | no | no |

Switch profile via `SPRING_PROFILES_ACTIVE` env variable or `--spring.profiles.active=prod`.

---

## Troubleshooting

**`FATAL: password authentication failed for user "postgres"`**
The credentials in `.env` don't match the running PostgreSQL. Docker default is `postgres`/`postgres`; Homebrew installs often use your OS username with no password.

**`Flyway checksum mismatch on Vxx`**
A migration file was edited after it ran. Repair with:
```bash
./mvnw flyway:repair -Dflyway.url=... -Dflyway.user=... -Dflyway.password=...
```
Or (local only) set `spring.flyway.validate-on-migrate=false` in `application-local.properties` — already done.

**Testcontainers: `Could not find a valid Docker environment`**
Docker must be running. On macOS, start Docker Desktop. Testcontainers uses Ryuk for automatic container cleanup.

**Port 8080 already in use**
Set `SERVER_PORT=8081` in `.env` or pass `-Dserver.port=8081` to Maven.

**`No customer profile for this account` (403 on `/api/me/**`)**
The user was created without a linked `customers` row. Use `POST /api/admin/customers` to create one, or ensure the seed data migration ran (requires `local` or `dev` profile).
