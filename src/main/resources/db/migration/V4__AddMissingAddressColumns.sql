-- Add state column to addresses if missing (idempotent for PostgreSQL).
ALTER TABLE addresses ADD COLUMN IF NOT EXISTS state VARCHAR(255) NULL;
