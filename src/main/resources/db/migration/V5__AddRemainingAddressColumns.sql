-- Add remaining Address columns if missing (addresses table may have been created by Hibernate with different column names).
-- Each block is idempotent: only adds the column when it does not exist.

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'addresses' AND COLUMN_NAME = 'street_line1') > 0,
    'SELECT 1',
    'ALTER TABLE addresses ADD COLUMN street_line1 VARCHAR(255) NULL'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'addresses' AND COLUMN_NAME = 'street_line2') > 0,
    'SELECT 1',
    'ALTER TABLE addresses ADD COLUMN street_line2 VARCHAR(255) NULL'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'addresses' AND COLUMN_NAME = 'city') > 0,
    'SELECT 1',
    'ALTER TABLE addresses ADD COLUMN city VARCHAR(255) NULL'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'addresses' AND COLUMN_NAME = 'postal_code') > 0,
    'SELECT 1',
    'ALTER TABLE addresses ADD COLUMN postal_code VARCHAR(255) NULL'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'addresses' AND COLUMN_NAME = 'country') > 0,
    'SELECT 1',
    'ALTER TABLE addresses ADD COLUMN country VARCHAR(255) NULL'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
