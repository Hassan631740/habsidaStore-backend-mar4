-- Add quantity to cart_items if missing (idempotent for PostgreSQL).
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS quantity INT NULL;
