-- Delivery areas: add delivery fee per zone
ALTER TABLE store_delivery_areas ADD COLUMN IF NOT EXISTS fee DECIMAL(19, 4) NULL;

-- Work hours: add last-order cutoff time (orders placed after this time are deferred to next day)
ALTER TABLE store_hours ADD COLUMN IF NOT EXISTS last_order_cutoff_time TIME NULL;

-- Stores: add location field for geographic filtering
ALTER TABLE stores ADD COLUMN IF NOT EXISTS location VARCHAR(255) NULL;
