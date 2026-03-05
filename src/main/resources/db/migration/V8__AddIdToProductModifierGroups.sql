-- product_modifier_groups may have been created by Hibernate as a join table without id column.
-- Add id as primary key when missing (drop existing composite PK first).

-- Step 1: Drop existing primary key if present (so we can add id as new PK)
DROP PROCEDURE IF EXISTS drop_pmg_pk;
DELIMITER //
CREATE PROCEDURE drop_pmg_pk()
BEGIN
  IF (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_modifier_groups' AND CONSTRAINT_TYPE = 'PRIMARY KEY') > 0
  THEN
    ALTER TABLE product_modifier_groups DROP PRIMARY KEY;
  END IF;
END//
DELIMITER ;
CALL drop_pmg_pk();
DROP PROCEDURE drop_pmg_pk;

-- Step 2: Add id column and primary key if missing
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_modifier_groups' AND COLUMN_NAME = 'id') > 0,
    'SELECT 1',
    'ALTER TABLE product_modifier_groups ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
