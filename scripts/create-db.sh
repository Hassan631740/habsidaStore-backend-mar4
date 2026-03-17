#!/usr/bin/env bash
# Creates the habsida_store database if it does not exist.
#
# Usage:
#   ./scripts/create-db.sh                    # will prompt for password if required
#   PGPASSWORD=yourpass ./scripts/create-db.sh   # supply password (no prompt)
#
# If you get "no password supplied": set a password, then run with PGPASSWORD:
#   psql -U postgres -d postgres -c "ALTER USER hassankoroma PASSWORD 'postgres';"
#   PGPASSWORD=postgres ./scripts/create-db.sh

set -e
HOST="${PGHOST:-}"
PORT="${PGPORT:-5432}"
USER="${PGUSER:-$(whoami)}"
DB_NAME="habsida_store"

# Use Unix socket when no host set → peer auth, no password
run_psql() {
  if [[ -n "$HOST" ]]; then
    psql -h "$HOST" -p "$PORT" -U "$USER" "$@"
  else
    psql -p "$PORT" -U "$USER" "$@"
  fi
}
run_createdb() {
  if [[ -n "$HOST" ]]; then
    createdb -h "$HOST" -p "$PORT" -U "$USER" "$@"
  else
    createdb -p "$PORT" -U "$USER" "$@"
  fi
}

if run_psql -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$DB_NAME'" | grep -q 1; then
  echo "Database '$DB_NAME' already exists."
else
  echo "Creating database '$DB_NAME'..."
  run_createdb "$DB_NAME"
  echo "Database '$DB_NAME' created."
fi
