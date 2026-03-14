ALTER TABLE products ADD COLUMN IF NOT EXISTS available_for_order BOOLEAN DEFAULT true;
