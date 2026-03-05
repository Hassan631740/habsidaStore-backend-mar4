package com.habsida.store.flyway;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Drops all FKs, alters all id/FK columns to BIGINT, re-adds FKs.
 * Fixes schema validation when DB was created with INT (e.g. by Hibernate).
 * <p>
 * PK columns: MODIFY id BIGINT NOT NULL AUTO_INCREMENT (after DROP/ADD PK to avoid MySQL 1171).
 * FK columns: MODIFY ... BIGINT NULL (or NOT NULL to match business logic).
 * <p>
 * If this migration failed and Flyway is dirty, repair before re-running:
 * DELETE FROM flyway_schema_history WHERE success = 0;
 */
public class V3__AlterIdsToBigint extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        // Do NOT close the connection - Flyway needs it after the migration.
        Connection conn = context.getConnection();
        setForeignKeyChecks(conn, false);
        dropAllForeignKeys(conn);
        runAlterStatements(conn);
        setForeignKeyChecks(conn, true);
        addForeignKeys(conn);
    }

    private void setForeignKeyChecks(Connection conn, boolean on) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS = " + (on ? "1" : "0"));
        }
    }

    private void dropAllForeignKeys(Connection conn) throws SQLException {
        String catalog = conn.getCatalog();
        String sql = "SELECT TABLE_NAME, CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "WHERE TABLE_SCHEMA = ? AND REFERENCED_TABLE_NAME IS NOT NULL " +
                "GROUP BY TABLE_NAME, CONSTRAINT_NAME";
        List<String> alterStatements = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, catalog != null ? catalog : conn.getSchema());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String table = rs.getString("TABLE_NAME");
                    String constraint = rs.getString("CONSTRAINT_NAME");
                    alterStatements.add("ALTER TABLE `" + table + "` DROP FOREIGN KEY `" + constraint + "`");
                }
            }
        }
        try (Statement st = conn.createStatement()) {
            for (String alter : alterStatements) {
                st.execute(alter);
            }
        }
    }

    private void runAlterStatements(Connection conn) throws SQLException {
        // MySQL 1075: AUTO_INCREMENT column must be defined as a key. So we cannot have
        // AUTO_INCREMENT on id after DROP PRIMARY KEY. Sequence per table:
        // 1) MODIFY id BIGINT NOT NULL (change type, drop AUTO_INCREMENT while still PK)
        // 2) DROP PRIMARY KEY
        // 3) ADD PRIMARY KEY (id)
        // 4) MODIFY id BIGINT NOT NULL AUTO_INCREMENT (re-add AUTO_INCREMENT now that id is a key)
        String[] tablesWithIdPk = {
                "addresses", "roles", "categories", "users", "stores", "customers",
                "modifier_groups", "modifier_options", "products", "product_images", "product_modifier_groups",
                "carts", "cart_items", "orders", "order_items", "order_item_modifiers",
                "order_addresses", "order_payments", "fulfillment",
                "user_roles", "user_store_access", "customer_addresses",
                "store_hours", "store_breaks", "store_delivery_settings", "store_delivery_areas", "store_delivery_restrictions"
        };
        try (Statement st = conn.createStatement()) {
            for (String table : tablesWithIdPk) {
                // Step 1: ensure id is BIGINT NOT NULL (and drop AUTO_INCREMENT so we can drop PK)
                try {
                    st.execute("ALTER TABLE `" + table + "` MODIFY COLUMN id BIGINT NOT NULL");
                } catch (SQLException e) {
                    if (!isAcceptableAlterError(e)) throw e;
                }
                // Step 2: drop PK (ignore if already dropped from previous failed run)
                try {
                    st.execute("ALTER TABLE `" + table + "` DROP PRIMARY KEY");
                } catch (SQLException e) {
                    if (!e.getMessage().contains("Can't DROP") && !e.getMessage().contains("check that it exists")) throw e;
                }
                // Step 3: re-assert NOT NULL then add PK (MySQL 1171: PK columns must be NOT NULL)
                try {
                    st.execute("ALTER TABLE `" + table + "` MODIFY COLUMN id BIGINT NOT NULL");
                } catch (SQLException e) {
                    if (!isAcceptableAlterError(e)) throw e;
                }
                try {
                    st.execute("ALTER TABLE `" + table + "` ADD PRIMARY KEY (id)");
                } catch (SQLException e) {
                    if (!isAcceptableAlterError(e)) throw e;
                }
                // Step 4: re-add AUTO_INCREMENT (column is a key again)
                try {
                    st.execute("ALTER TABLE `" + table + "` MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT");
                } catch (SQLException e) {
                    if (!isAcceptableAlterError(e)) throw e;
                }
            }
            // FK columns: explicit BIGINT NULL (or NOT NULL if relationship is required)
            String[] fkAlters = {
                    "ALTER TABLE categories MODIFY COLUMN parent_id BIGINT NULL",
                    "ALTER TABLE stores MODIFY COLUMN address_id BIGINT NULL",
                    "ALTER TABLE customers MODIFY COLUMN user_id BIGINT NULL",
                    "ALTER TABLE modifier_groups MODIFY COLUMN store_id BIGINT NULL",
                    "ALTER TABLE modifier_options MODIFY COLUMN modifier_group_id BIGINT NULL",
                    "ALTER TABLE products MODIFY COLUMN category_id BIGINT NULL",
                    "ALTER TABLE products MODIFY COLUMN store_id BIGINT NULL",
                    "ALTER TABLE product_images MODIFY COLUMN product_id BIGINT NULL",
                    "ALTER TABLE product_modifier_groups MODIFY COLUMN product_id BIGINT NULL",
                    "ALTER TABLE product_modifier_groups MODIFY COLUMN modifier_group_id BIGINT NULL",
                    "ALTER TABLE carts MODIFY COLUMN customer_id BIGINT NULL",
                    "ALTER TABLE cart_items MODIFY COLUMN cart_id BIGINT NULL",
                    "ALTER TABLE cart_items MODIFY COLUMN product_id BIGINT NULL",
                    "ALTER TABLE orders MODIFY COLUMN customer_id BIGINT NULL",
                    "ALTER TABLE order_items MODIFY COLUMN order_id BIGINT NULL",
                    "ALTER TABLE order_items MODIFY COLUMN product_id BIGINT NULL",
                    "ALTER TABLE order_item_modifiers MODIFY COLUMN order_item_id BIGINT NULL",
                    "ALTER TABLE order_item_modifiers MODIFY COLUMN modifier_option_id BIGINT NULL",
                    "ALTER TABLE order_addresses MODIFY COLUMN order_id BIGINT NULL",
                    "ALTER TABLE order_addresses MODIFY COLUMN address_id BIGINT NULL",
                    "ALTER TABLE order_payments MODIFY COLUMN order_id BIGINT NULL",
                    "ALTER TABLE fulfillment MODIFY COLUMN order_id BIGINT NULL",
                    "ALTER TABLE user_roles MODIFY COLUMN user_id BIGINT NULL",
                    "ALTER TABLE user_roles MODIFY COLUMN role_id BIGINT NULL",
                    "ALTER TABLE user_store_access MODIFY COLUMN user_id BIGINT NULL",
                    "ALTER TABLE user_store_access MODIFY COLUMN store_id BIGINT NULL",
                    "ALTER TABLE customer_addresses MODIFY COLUMN customer_id BIGINT NULL",
                    "ALTER TABLE customer_addresses MODIFY COLUMN address_id BIGINT NULL",
                    "ALTER TABLE store_hours MODIFY COLUMN store_id BIGINT NULL",
                    "ALTER TABLE store_breaks MODIFY COLUMN store_id BIGINT NULL",
                    "ALTER TABLE store_delivery_settings MODIFY COLUMN store_id BIGINT NULL",
                    "ALTER TABLE store_delivery_areas MODIFY COLUMN store_id BIGINT NULL",
                    "ALTER TABLE store_delivery_restrictions MODIFY COLUMN store_id BIGINT NULL",
            };
            for (String sql : fkAlters) {
                try {
                    st.execute(sql);
                } catch (SQLException e) {
                    if (!isAcceptableAlterError(e)) throw e;
                }
            }
        }
    }

    private boolean isAcceptableAlterError(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        // Skip when idempotent or table/column missing
        return msg.contains("Duplicate column") || msg.contains("Unknown column")
                || msg.contains("doesn't exist") || msg.contains("check that it exists")
                || msg.contains("Duplicate key") || msg.contains("Multiple primary key")
                || msg.contains("Can't DROP");
    }

    private void addForeignKeys(Connection conn) throws SQLException {
        String[] fks = {
                "ALTER TABLE categories ADD CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories (id)",
                "ALTER TABLE stores ADD CONSTRAINT fk_stores_address FOREIGN KEY (address_id) REFERENCES addresses (id)",
                "ALTER TABLE customers ADD CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users (id)",
                "ALTER TABLE modifier_groups ADD CONSTRAINT fk_modifier_groups_store FOREIGN KEY (store_id) REFERENCES stores (id)",
                "ALTER TABLE modifier_options ADD CONSTRAINT fk_modifier_options_group FOREIGN KEY (modifier_group_id) REFERENCES modifier_groups (id)",
                "ALTER TABLE products ADD CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)",
                "ALTER TABLE products ADD CONSTRAINT fk_products_store FOREIGN KEY (store_id) REFERENCES stores (id)",
                "ALTER TABLE product_images ADD CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id)",
                "ALTER TABLE product_modifier_groups ADD CONSTRAINT fk_pmg_product FOREIGN KEY (product_id) REFERENCES products (id)",
                "ALTER TABLE product_modifier_groups ADD CONSTRAINT fk_pmg_modifier_group FOREIGN KEY (modifier_group_id) REFERENCES modifier_groups (id)",
                "ALTER TABLE carts ADD CONSTRAINT fk_carts_customer FOREIGN KEY (customer_id) REFERENCES customers (id)",
                "ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id)",
                "ALTER TABLE cart_items ADD CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id)",
                "ALTER TABLE orders ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id)",
                "ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id)",
                "ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)",
                "ALTER TABLE order_item_modifiers ADD CONSTRAINT fk_oim_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id)",
                "ALTER TABLE order_item_modifiers ADD CONSTRAINT fk_oim_modifier_option FOREIGN KEY (modifier_option_id) REFERENCES modifier_options (id)",
                "ALTER TABLE order_addresses ADD CONSTRAINT fk_order_addresses_order FOREIGN KEY (order_id) REFERENCES orders (id)",
                "ALTER TABLE order_addresses ADD CONSTRAINT fk_order_addresses_address FOREIGN KEY (address_id) REFERENCES addresses (id)",
                "ALTER TABLE order_payments ADD CONSTRAINT fk_order_payments_order FOREIGN KEY (order_id) REFERENCES orders (id)",
                "ALTER TABLE fulfillment ADD CONSTRAINT fk_fulfillment_order FOREIGN KEY (order_id) REFERENCES orders (id)",
                "ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id)",
                "ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)",
                "ALTER TABLE user_store_access ADD CONSTRAINT fk_usa_user FOREIGN KEY (user_id) REFERENCES users (id)",
                "ALTER TABLE user_store_access ADD CONSTRAINT fk_usa_store FOREIGN KEY (store_id) REFERENCES stores (id)",
                "ALTER TABLE customer_addresses ADD CONSTRAINT fk_customer_addresses_customer FOREIGN KEY (customer_id) REFERENCES customers (id)",
                "ALTER TABLE customer_addresses ADD CONSTRAINT fk_customer_addresses_address FOREIGN KEY (address_id) REFERENCES addresses (id)",
                "ALTER TABLE store_hours ADD CONSTRAINT fk_store_hours_store FOREIGN KEY (store_id) REFERENCES stores (id)",
                "ALTER TABLE store_breaks ADD CONSTRAINT fk_store_breaks_store FOREIGN KEY (store_id) REFERENCES stores (id)",
                "ALTER TABLE store_delivery_settings ADD CONSTRAINT fk_store_delivery_settings_store FOREIGN KEY (store_id) REFERENCES stores (id)",
                "ALTER TABLE store_delivery_areas ADD CONSTRAINT fk_store_delivery_areas_store FOREIGN KEY (store_id) REFERENCES stores (id)",
                "ALTER TABLE store_delivery_restrictions ADD CONSTRAINT fk_store_delivery_restrictions_store FOREIGN KEY (store_id) REFERENCES stores (id)",
        };
        try (Statement st = conn.createStatement()) {
            for (String sql : fks) {
                try {
                    st.execute(sql);
                } catch (SQLException e) {
                    String msg = e.getMessage();
                    if (msg == null) throw e;
                    // Skip if FK already exists or column doesn't exist (e.g. DB created by Hibernate with different column names)
                    if (msg.contains("Duplicate foreign key")) continue;
                    if (msg.contains("doesn't exist in table") || msg.contains("Unknown column") || e.getErrorCode() == 1072) continue;
                    throw e;
                }
            }
        }
    }
}
