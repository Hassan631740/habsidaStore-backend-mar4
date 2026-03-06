-- product_modifier_groups: ensure id column and primary key (idempotent for PostgreSQL).
-- Drop existing primary key (composite or single), add id if missing, then set PK on id.

ALTER TABLE product_modifier_groups DROP CONSTRAINT IF EXISTS product_modifier_groups_pkey;
ALTER TABLE product_modifier_groups ADD COLUMN IF NOT EXISTS id BIGSERIAL;
ALTER TABLE product_modifier_groups ADD PRIMARY KEY (id);
