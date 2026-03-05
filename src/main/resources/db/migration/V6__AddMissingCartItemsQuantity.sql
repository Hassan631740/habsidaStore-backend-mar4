-- Add quantity to cart_items if missing (Hibernate-created schema may use different column names).
-- Idempotent: only adds the column when it does not exist.

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cart_items' AND COLUMN_NAME = 'quantity') > 0,
    'SELECT 1',
    'ALTER TABLE cart_items ADD COLUMN quantity INT NULL'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
