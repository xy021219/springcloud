CREATE TABLE IF NOT EXISTS seckill_product (
  product_code varchar(32) NOT NULL,
  product_name varchar(64) NOT NULL,
  db_stock int NOT NULL,
  PRIMARY KEY (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO seckill_product (product_code, product_name, db_stock)
SELECT 'P1010', CONVERT(UNHEX('E59586E5938131303130') USING utf8mb4), stock
FROM seckill_activity
WHERE activity_id = 1
  AND NOT EXISTS (SELECT 1 FROM seckill_product WHERE product_code = 'P1010');

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'ry-cloud' AND TABLE_NAME = 'seckill_activity' AND COLUMN_NAME = 'product_code') = 0,
  'ALTER TABLE seckill_activity ADD COLUMN product_code varchar(32) NULL',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE seckill_activity SET product_code = 'P1010' WHERE activity_id = 1 AND (product_code IS NULL OR product_code = '');

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'ry-cloud' AND TABLE_NAME = 'seckill_order' AND COLUMN_NAME = 'order_no') = 0,
  'ALTER TABLE seckill_order ADD COLUMN order_no varchar(32) NULL',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'ry-cloud' AND TABLE_NAME = 'seckill_order' AND COLUMN_NAME = 'product_code') = 0,
  'ALTER TABLE seckill_order ADD COLUMN product_code varchar(32) NULL',
  'SELECT 1'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE seckill_order SET product_code = 'P1010' WHERE activity_id = 1 AND (product_code IS NULL OR product_code = '');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 122, CONVERT(UNHEX('E8AEA2E58D95') USING utf8mb4), 3, 8, 'orders', 'tool/orders/index', '', '', 1, 0, 'C', '0', '0', 'tool:seckill:list', 'list', 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 122);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 122 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 122);
