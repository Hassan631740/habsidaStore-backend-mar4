-- Customer account status (admin-managed): ACTIVE | SUSPENDED
ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE';
