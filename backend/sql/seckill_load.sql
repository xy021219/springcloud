INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 121, CONVERT(UNHEX('E5B9B6E58F91E6B58BE8AF95') USING utf8mb4), 3, 7, 'loadtest', 'tool/loadtest/index', '', '', 1, 0, 'C', '0', '0', 'tool:seckill:list', 'example', 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 121);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, 121 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 121);
