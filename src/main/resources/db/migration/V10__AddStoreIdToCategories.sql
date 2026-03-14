-- Store-scoped categories
ALTER TABLE categories ADD COLUMN IF NOT EXISTS store_id BIGINT NULL;
