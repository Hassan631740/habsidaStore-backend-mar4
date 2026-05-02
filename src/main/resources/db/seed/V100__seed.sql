-- Demo / development seed data.
-- Activated only in the local and dev profiles via spring.flyway.locations.
-- All inserts are idempotent (ON CONFLICT DO NOTHING or NOT EXISTS guards).
--
-- Credentials (BCrypt cost 10):
--   admin@habsida.com      / admin123
--   merchant1@habsida.com  / merchant123
--   merchant2@habsida.com  / merchant123
--   customer@habsida.com   / customer123

-- ---- roles ----------------------------------------------------------------
INSERT INTO roles (name) VALUES ('ADMIN')    ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('MERCHANT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('CUSTOMER') ON CONFLICT (name) DO NOTHING;

-- ---- users ----------------------------------------------------------------
INSERT INTO users (email, password_hash, created_at, updated_at)
VALUES ('admin@habsida.com',
        '$2b$10$3KSUkbG.VAwgvONaC/hwwegKitvTd3DmLUnCwaNyLB1NcTtY89jaa',
        NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, created_at, updated_at)
VALUES ('merchant1@habsida.com',
        '$2b$10$iOrqYwaZrsnzcERU9kU3l.D3RKiXVtXQn71ORzEpjQXg38QBj1qlO',
        NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, created_at, updated_at)
VALUES ('merchant2@habsida.com',
        '$2b$10$iOrqYwaZrsnzcERU9kU3l.D3RKiXVtXQn71ORzEpjQXg38QBj1qlO',
        NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, created_at, updated_at)
VALUES ('customer@habsida.com',
        '$2b$10$9dhN8onRiJ7cHz86gtwT8evOhfvwIc98FwJuWvzmJNZWYFYhPbqcK',
        NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- ---- user roles -----------------------------------------------------------
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@habsida.com' AND r.name = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur2
      WHERE ur2.user_id = u.id AND ur2.role_id = r.id);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'merchant1@habsida.com' AND r.name = 'MERCHANT'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur2
      WHERE ur2.user_id = u.id AND ur2.role_id = r.id);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'merchant2@habsida.com' AND r.name = 'MERCHANT'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur2
      WHERE ur2.user_id = u.id AND ur2.role_id = r.id);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'customer@habsida.com' AND r.name = 'CUSTOMER'
  AND NOT EXISTS (
      SELECT 1 FROM user_roles ur2
      WHERE ur2.user_id = u.id AND ur2.role_id = r.id);

-- ---- stores ---------------------------------------------------------------
INSERT INTO stores (name, status, created_at, updated_at)
SELECT 'Coffee House', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM stores WHERE name = 'Coffee House');

INSERT INTO stores (name, status, created_at, updated_at)
SELECT 'Tea Garden', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM stores WHERE name = 'Tea Garden');

-- ---- user → store access --------------------------------------------------
INSERT INTO user_store_access (user_id, store_id)
SELECT u.id, s.id FROM users u, stores s
WHERE u.email = 'merchant1@habsida.com' AND s.name = 'Coffee House'
  AND NOT EXISTS (
      SELECT 1 FROM user_store_access usa2
      WHERE usa2.user_id = u.id AND usa2.store_id = s.id);

INSERT INTO user_store_access (user_id, store_id)
SELECT u.id, s.id FROM users u, stores s
WHERE u.email = 'merchant2@habsida.com' AND s.name = 'Tea Garden'
  AND NOT EXISTS (
      SELECT 1 FROM user_store_access usa2
      WHERE usa2.user_id = u.id AND usa2.store_id = s.id);

-- ---- categories -----------------------------------------------------------
INSERT INTO categories (name, slug, store_id)
SELECT 'Hot Drinks', 'hot-drinks', s.id FROM stores s WHERE s.name = 'Coffee House'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.slug = 'hot-drinks' AND c.store_id = s.id);

INSERT INTO categories (name, slug, store_id)
SELECT 'Cold Drinks', 'cold-drinks', s.id FROM stores s WHERE s.name = 'Coffee House'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.slug = 'cold-drinks' AND c.store_id = s.id);

INSERT INTO categories (name, slug, store_id)
SELECT 'Snacks', 'snacks-coffee', s.id FROM stores s WHERE s.name = 'Coffee House'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.slug = 'snacks-coffee' AND c.store_id = s.id);

INSERT INTO categories (name, slug, store_id)
SELECT 'Teas', 'teas', s.id FROM stores s WHERE s.name = 'Tea Garden'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.slug = 'teas' AND c.store_id = s.id);

INSERT INTO categories (name, slug, store_id)
SELECT 'Pastries', 'pastries', s.id FROM stores s WHERE s.name = 'Tea Garden'
  AND NOT EXISTS (SELECT 1 FROM categories c WHERE c.slug = 'pastries' AND c.store_id = s.id);

-- ---- products (Coffee House) ----------------------------------------------
INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Espresso', 'Single shot, bold and intense', 2.50,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Coffee House' AND c.slug = 'hot-drinks'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Espresso' AND p.store_id = s.id);

INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Cappuccino', 'Espresso with steamed milk foam', 3.50,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Coffee House' AND c.slug = 'hot-drinks'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Cappuccino' AND p.store_id = s.id);

INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Latte', 'Espresso with a lot of steamed milk', 4.00,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Coffee House' AND c.slug = 'hot-drinks'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Latte' AND p.store_id = s.id);

INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Iced Americano', 'Cold espresso over ice', 3.50,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Coffee House' AND c.slug = 'cold-drinks'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Iced Americano' AND p.store_id = s.id);

INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Cold Brew', '12-hour cold-extracted coffee', 4.50,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Coffee House' AND c.slug = 'cold-drinks'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Cold Brew' AND p.store_id = s.id);

INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Butter Croissant', 'Freshly baked, flaky pastry', 2.00,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Coffee House' AND c.slug = 'snacks-coffee'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Butter Croissant' AND p.store_id = s.id);

-- ---- products (Tea Garden) ------------------------------------------------
INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Earl Grey', 'Classic bergamot black tea', 3.00,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Tea Garden' AND c.slug = 'teas'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Earl Grey' AND p.store_id = s.id);

INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Matcha Latte', 'Ceremonial-grade matcha with oat milk', 4.50,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Tea Garden' AND c.slug = 'teas'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Matcha Latte' AND p.store_id = s.id);

INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Jasmine Green Tea', 'Fragrant jasmine-infused green tea', 3.50,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Tea Garden' AND c.slug = 'teas'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Jasmine Green Tea' AND p.store_id = s.id);

INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Almond Croissant', 'Filled with almond cream', 3.00,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Tea Garden' AND c.slug = 'pastries'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Almond Croissant' AND p.store_id = s.id);

INSERT INTO products (name, description, price, category_id, store_id, available_for_order, created_at, updated_at)
SELECT 'Scone', 'Traditional British scone with clotted cream', 2.50,
       c.id, s.id, true, NOW(), NOW()
FROM stores s JOIN categories c ON c.store_id = s.id
WHERE s.name = 'Tea Garden' AND c.slug = 'pastries'
  AND NOT EXISTS (SELECT 1 FROM products p WHERE p.name = 'Scone' AND p.store_id = s.id);

-- ---- customer profile -----------------------------------------------------
INSERT INTO customers (user_id, first_name, last_name, phone, status, created_at, updated_at)
SELECT u.id, 'Demo', 'Customer', '+1-555-0100', 'ACTIVE', NOW(), NOW()
FROM users u WHERE u.email = 'customer@habsida.com'
  AND NOT EXISTS (SELECT 1 FROM customers c WHERE c.user_id = u.id);

-- ---- sample orders --------------------------------------------------------
-- Order 1: two Espressos at Coffee House, status NEW
INSERT INTO orders (store_id, customer_id, status, order_type, total_amount, created_at, updated_at)
SELECT s.id, c.id, 'NEW', 'DELIVERY', 5.00, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'
FROM stores s, customers c JOIN users u ON u.id = c.user_id
WHERE s.name = 'Coffee House' AND u.email = 'customer@habsida.com'
  AND NOT EXISTS (
      SELECT 1 FROM orders o WHERE o.store_id = s.id AND o.customer_id = c.id AND o.total_amount = 5.00);

INSERT INTO order_items (order_id, product_id, product_name_snapshot, unit_price_snapshot, quantity, created_at, updated_at)
SELECT o.id, p.id, p.name, p.price, 2, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'
FROM orders o
JOIN stores s ON s.id = o.store_id
JOIN products p ON p.store_id = s.id AND p.name = 'Espresso'
WHERE s.name = 'Coffee House' AND o.total_amount = 5.00
  AND NOT EXISTS (SELECT 1 FROM order_items oi WHERE oi.order_id = o.id AND oi.product_id = p.id);

-- Order 2: Matcha Latte + Scone at Tea Garden, status ACCEPTED
INSERT INTO orders (store_id, customer_id, status, order_type, total_amount, accepted_at, created_at, updated_at)
SELECT s.id, c.id, 'ACCEPTED', 'PICKUP', 7.00, NOW() - INTERVAL '30 minutes',
       NOW() - INTERVAL '45 minutes', NOW() - INTERVAL '30 minutes'
FROM stores s, customers c JOIN users u ON u.id = c.user_id
WHERE s.name = 'Tea Garden' AND u.email = 'customer@habsida.com'
  AND NOT EXISTS (
      SELECT 1 FROM orders o WHERE o.store_id = s.id AND o.customer_id = c.id AND o.total_amount = 7.00);

INSERT INTO order_items (order_id, product_id, product_name_snapshot, unit_price_snapshot, quantity, created_at, updated_at)
SELECT o.id, p.id, p.name, p.price, 1, NOW() - INTERVAL '45 minutes', NOW() - INTERVAL '45 minutes'
FROM orders o
JOIN stores s ON s.id = o.store_id
JOIN products p ON p.store_id = s.id AND p.name = 'Matcha Latte'
WHERE s.name = 'Tea Garden' AND o.total_amount = 7.00
  AND NOT EXISTS (SELECT 1 FROM order_items oi WHERE oi.order_id = o.id AND oi.product_id = p.id);

INSERT INTO order_items (order_id, product_id, product_name_snapshot, unit_price_snapshot, quantity, created_at, updated_at)
SELECT o.id, p.id, p.name, p.price, 1, NOW() - INTERVAL '45 minutes', NOW() - INTERVAL '45 minutes'
FROM orders o
JOIN stores s ON s.id = o.store_id
JOIN products p ON p.store_id = s.id AND p.name = 'Scone'
WHERE s.name = 'Tea Garden' AND o.total_amount = 7.00
  AND NOT EXISTS (SELECT 1 FROM order_items oi WHERE oi.order_id = o.id AND oi.product_id = p.id);
