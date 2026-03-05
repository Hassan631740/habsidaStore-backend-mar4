-- Add state column to addresses if missing (e.g. when DB was created by Hibernate).
-- Idempotent: only runs ALTER when the column does not exist.

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'addresses' AND COLUMN_NAME = 'state') > 0,
    'SELECT 1',
    'ALTER TABLE addresses ADD COLUMN state VARCHAR(255) NULL AFTER city'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
