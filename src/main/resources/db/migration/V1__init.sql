-- Habsida Store schema: tables matching JPA entities (PostgreSQL)

-- 1. Independent tables
CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    street_line1 VARCHAR(255),
    street_line2 VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    postal_code VARCHAR(255),
    country VARCHAR(255)
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    slug VARCHAR(255),
    parent_id BIGINT NULL,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories (id)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6)
);

-- 2. Store and related (depend on addresses, users)
CREATE TABLE stores (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    address_id BIGINT NULL,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT fk_stores_address FOREIGN KEY (address_id) REFERENCES addresses (id)
);

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone VARCHAR(255),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- 3. Modifiers (store -> modifier_groups -> modifier_options)
CREATE TABLE modifier_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    store_id BIGINT NULL,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT fk_modifier_groups_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE modifier_options (
    id BIGSERIAL PRIMARY KEY,
    modifier_group_id BIGINT NULL,
    name VARCHAR(255),
    price_adjustment DECIMAL(19, 4),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT fk_modifier_options_group FOREIGN KEY (modifier_group_id) REFERENCES modifier_groups (id)
);

-- 4. Products and links
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(1000),
    price DECIMAL(19, 4),
    category_id BIGINT NULL,
    store_id BIGINT NULL,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_products_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NULL,
    url VARCHAR(500),
    sort_order INT,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE product_modifier_groups (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NULL,
    modifier_group_id BIGINT NULL,
    CONSTRAINT fk_pmg_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_pmg_modifier_group FOREIGN KEY (modifier_group_id) REFERENCES modifier_groups (id)
);

-- 5. Cart
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NULL,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NULL,
    product_id BIGINT NULL,
    quantity INT,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- 6. Orders and related
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NULL,
    status VARCHAR(255),
    total_amount DECIMAL(19, 4),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NULL,
    product_id BIGINT NULL,
    quantity INT,
    price DECIMAL(19, 4),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE TABLE order_item_modifiers (
    id BIGSERIAL PRIMARY KEY,
    order_item_id BIGINT NULL,
    modifier_option_id BIGINT NULL,
    price DECIMAL(19, 4),
    CONSTRAINT fk_oim_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id),
    CONSTRAINT fk_oim_modifier_option FOREIGN KEY (modifier_option_id) REFERENCES modifier_options (id)
);

CREATE TABLE order_addresses (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NULL,
    address_id BIGINT NULL,
    CONSTRAINT fk_order_addresses_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_addresses_address FOREIGN KEY (address_id) REFERENCES addresses (id)
);

CREATE TABLE order_payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NULL,
    amount DECIMAL(19, 4),
    payment_method VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT fk_order_payments_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE fulfillment (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NULL,
    status VARCHAR(255),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT fk_fulfillment_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

-- 7. User/role and user/store
CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NULL,
    role_id BIGINT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE user_store_access (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NULL,
    store_id BIGINT NULL,
    CONSTRAINT fk_usa_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_usa_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

-- 8. Customer addresses
CREATE TABLE customer_addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NULL,
    address_id BIGINT NULL,
    CONSTRAINT fk_customer_addresses_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_customer_addresses_address FOREIGN KEY (address_id) REFERENCES addresses (id)
);

-- 9. Store configuration tables
CREATE TABLE store_hours (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NULL,
    day_of_week INT,
    open_time TIME,
    close_time TIME,
    CONSTRAINT fk_store_hours_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE store_breaks (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NULL,
    start_time TIME,
    end_time TIME,
    CONSTRAINT fk_store_breaks_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE store_delivery_settings (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NULL,
    delivery_fee DECIMAL(19, 4),
    min_order_amount DECIMAL(19, 4),
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    CONSTRAINT fk_store_delivery_settings_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE store_delivery_areas (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NULL,
    name VARCHAR(255),
    CONSTRAINT fk_store_delivery_areas_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE store_delivery_restrictions (
    id BIGSERIAL PRIMARY KEY,
    store_id BIGINT NULL,
    type VARCHAR(255),
    value VARCHAR(255),
    CONSTRAINT fk_store_delivery_restrictions_store FOREIGN KEY (store_id) REFERENCES stores (id)
);
