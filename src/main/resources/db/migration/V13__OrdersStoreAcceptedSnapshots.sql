-- Orders: store_id, accepted_at, reject_reason, notes
ALTER TABLE orders ADD COLUMN IF NOT EXISTS store_id BIGINT NULL;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS accepted_at TIMESTAMP(6) NULL;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP(6) NULL;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS reject_reason VARCHAR(500) NULL;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS notes VARCHAR(2000) NULL;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_orders_store') THEN
        ALTER TABLE orders ADD CONSTRAINT fk_orders_store FOREIGN KEY (store_id) REFERENCES stores (id);
    END IF;
END $$;

-- Order items: product_name_snapshot, unit_price_snapshot
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS product_name_snapshot VARCHAR(500) NULL;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS unit_price_snapshot DECIMAL(19, 4) NULL;

-- Order item modifiers: option_name_snapshot (price already stored as snapshot)
ALTER TABLE order_item_modifiers ADD COLUMN IF NOT EXISTS option_name_snapshot VARCHAR(500) NULL;
