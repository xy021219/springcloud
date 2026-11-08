CREATE TABLE IF NOT EXISTS seckill_activity (
  activity_id bigint NOT NULL,
  activity_name varchar(64) NOT NULL,
  stock int NOT NULL,
  PRIMARY KEY (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO seckill_activity (activity_id, activity_name, stock)
SELECT 1, CONVERT(UNHEX('E6BC94E7A4BAE59586E59381') USING utf8mb4), 10
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM seckill_activity WHERE activity_id = 1);

CREATE TABLE IF NOT EXISTS seckill_order (
  order_id bigint NOT NULL AUTO_INCREMENT,
  activity_id bigint NOT NULL,
  user_id bigint NOT NULL,
  request_id varchar(64) NOT NULL,
  create_time datetime NOT NULL,
  PRIMARY KEY (order_id),
  UNIQUE KEY uk_activity_user (activity_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 120, CONVERT(UNHEX('E7A792E69D80') USING utf8mb4), 3, 6, 'seckill', 'tool/seckill/index', '', '', 1, 0, 'C', '0', '0', 'tool:seckill:list', 'shopping', 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 120);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 120 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 120);
