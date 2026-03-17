# habsidaStore-backend-mar4

## Running the app locally (outside Docker)

1. **Copy env and set database credentials:**
   ```bash
   cp .env.example .env
   ```
2. **PostgreSQL must be running** and the credentials in `.env` must match it.

   - **If you use Docker only for PostgreSQL** (e.g. `docker run -d --name postgres-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=habsida_store -p 5432:5432 postgres:15`):
     - In `.env` use: `DATABASE_USERNAME=postgres`, `DATABASE_PASSWORD=postgres`, and `DATABASE_URL=jdbc:postgresql://localhost:5432/habsida_store` (or `localhost:5433` if you mapped to 5433).
   - **If you use Homebrew PostgreSQL** (local install):
     - In `.env` set `DATABASE_USERNAME` to your macOS username (e.g. `hassankoroma`) and `DATABASE_PASSWORD` to that user’s PostgreSQL password. Create the DB with `./scripts/create-db.sh` (use `PGPASSWORD=yourpass` if needed).

3. **Start the app** from your IDE or:
   ```bash
   ./mvnw spring-boot:run
   ```

**If you see** `FATAL: password authentication failed for user "postgres"`: the username/password in `.env` do not match the PostgreSQL server on the host/port in `DATABASE_URL`. Use the user and password that that server expects (Docker: `postgres`/`postgres`; Homebrew: often your OS user and its PG password).

---

## Docker (PostgreSQL + App)

Both the Spring Boot app and PostgreSQL run in Docker.

**Start everything:**

```bash
docker compose up -d
```

- **App:** http://localhost:8080  
- **PostgreSQL:** localhost:5432, database `habsida_store`, user/password `postgres` (override with `POSTGRES_USER` / `POSTGRES_PASSWORD` or a `.env` file).

**Rebuild the app after code changes:**

```bash
docker compose up -d --build
```

**View logs:**

```bash
docker compose logs -f app
docker compose logs -f postgres
```

**Stop and remove containers (data in `postgres_data` volume is kept):**

```bash
docker compose down
```