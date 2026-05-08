-- Performance indexes: FK columns and composite query patterns
-- All indexed columns are heavily queried on every authenticated request or order-list page.

-- ---- auth / security hot path ----
-- user_roles: looked up on every request to build the security principal
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id  ON user_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id  ON user_roles (role_id);

-- user_store_access: looked up on every merchant API request
CREATE INDEX IF NOT EXISTS idx_usa_user_id  ON user_store_access (user_id);
CREATE INDEX IF NOT EXISTS idx_usa_store_id ON user_store_access (store_id);

-- customers: looked up by JWT user ID on every customer request
CREATE INDEX IF NOT EXISTS idx_customers_user_id ON customers (user_id);

-- ---- orders (main dashboard table) ----
-- Simple FK indexes for joins/lookups
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders (customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_store_id    ON orders (store_id);

-- Composite for the merchant order list: WHERE store_id = ? AND status = ? ORDER BY created_at DESC
CREATE INDEX IF NOT EXISTS idx_orders_store_status_created
    ON orders (store_id, status, created_at DESC);

-- ---- order_items ----
CREATE INDEX IF NOT EXISTS idx_order_items_order_id   ON order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items (product_id);

-- ---- order_item_modifiers ----
CREATE INDEX IF NOT EXISTS idx_oim_order_item_id       ON order_item_modifiers (order_item_id);
CREATE INDEX IF NOT EXISTS idx_oim_modifier_option_id  ON order_item_modifiers (modifier_option_id);

-- ---- order_payments / fulfillment ----
CREATE INDEX IF NOT EXISTS idx_order_payments_order_id ON order_payments (order_id);
CREATE INDEX IF NOT EXISTS idx_fulfillment_order_id    ON fulfillment (order_id);

-- ---- products ----
CREATE INDEX IF NOT EXISTS idx_products_store_id    ON products (store_id);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products (category_id);

-- ---- modifiers ----
CREATE INDEX IF NOT EXISTS idx_modifier_groups_store_id        ON modifier_groups (store_id);
CREATE INDEX IF NOT EXISTS idx_modifier_options_group_id       ON modifier_options (modifier_group_id);
CREATE INDEX IF NOT EXISTS idx_product_modifier_groups_product ON product_modifier_groups (product_id);
CREATE INDEX IF NOT EXISTS idx_product_modifier_groups_group   ON product_modifier_groups (modifier_group_id);

-- ---- product_images ----
CREATE INDEX IF NOT EXISTS idx_product_images_product_id ON product_images (product_id);

-- ---- carts ----
CREATE INDEX IF NOT EXISTS idx_carts_customer_id      ON carts (customer_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id     ON cart_items (cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id  ON cart_items (product_id);

-- ---- categories ----
CREATE INDEX IF NOT EXISTS idx_categories_parent_id   ON categories (parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_store_id    ON categories (store_id);

-- ---- customer addresses ----
CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer_id ON customer_addresses (customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_addresses_address_id  ON customer_addresses (address_id);

-- ---- store configuration ----
CREATE INDEX IF NOT EXISTS idx_store_hours_store_id        ON store_hours (store_id);
CREATE INDEX IF NOT EXISTS idx_store_breaks_store_id       ON store_breaks (store_id);
CREATE INDEX IF NOT EXISTS idx_store_delivery_settings_sid ON store_delivery_settings (store_id);
CREATE INDEX IF NOT EXISTS idx_store_delivery_areas_sid    ON store_delivery_areas (store_id);
CREATE INDEX IF NOT EXISTS idx_store_delivery_restr_sid    ON store_delivery_restrictions (store_id);