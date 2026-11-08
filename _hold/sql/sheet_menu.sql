-- 系统工具下的改表菜单。重复执行前先确认 menu_id 118 未被占用。
insert into sys_menu values('118', '改表', '3', '4', 'sheet', 'tool/sheet/index', '', '', 1, 0, 'C', '0', '0', 'tool:sheet:list', 'excel', 'admin', sysdate(), '', null, '上传表格并修改两列');
insert into sys_menu values('1180', '表格预览', '118', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'tool:sheet:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1181', '表格导出', '118', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'tool:sheet:list', '#', 'admin', sysdate(), '', null, '');
insert into sys_role_menu values ('1', '118');
insert into sys_role_menu values ('1', '1180');
insert into sys_role_menu values ('1', '1181');
