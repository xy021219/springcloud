-- 系统工具下的开发报表。重复执行前先确认 menu_id 119 未被占用。
insert into sys_menu values('119', '开发报表', '3', '5', 'report', 'tool/report/index', '', '', 1, 0, 'C', '0', '0', 'tool:report:list', 'chart', 'admin', sysdate(), '', null, '根据模拟数据展示开发周报');
insert into sys_role_menu values ('1', '119');
