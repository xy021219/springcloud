2026-10-28 已在库 `ry-vue` 执行过下面的建库语句和两份官方脚本。不要重复执行，脚本里有 `drop table`。

## 目标

用若依官方前后端分离版换成项目底座。登录、退出、修改密码用它自带的页面和接口。表格上传改两列以后再加，这一轮不写。

## 范围

复用检索跳过：已经指定若依官方前后端分离版，本对话里也有结论，不再另找框架。

采用的版本（2026-10-28 从官方仓库核对）：

- 后端 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) `master`，版本 3.9.2，Spring Boot 4.1.0，JDK 17。代码放在 `backend/`。协议文件 `backend/LICENSE`（MIT，Copyright 2018 RuoYi）。
- 前端 [RuoYi-Vue3](https://github.com/yangzongzhuan/RuoYi-Vue3) `master`，版本 3.9.2，Vue 3.5、Vite 6、Element Plus。代码放在 `frontend/`。协议文件 `frontend/LICENSE`（同样的 MIT）。

不用 RuoYi-Vue-Plus，也不用 AgileBoot。不把 Vue2 的 `ruoyi-ui` 放进来。`master` 后端仓库本身已经没有 `ruoyi-ui`。

原来手写的上传改表程序从 `backend/`、`frontend/` 移走。表格规则仍记在 `docs/specs/2026-10-27-upload-edit-export.md`，以后作为若依里的一个功能加回去。`docs/specs/2026-10-27-auth.md` 不实施。

只改了本机连接，没有改登录逻辑：

- `backend/ruoyi-admin/src/main/resources/application-druid.yml`：默认连 `127.0.0.1:3306/ry-vue`，用户 `root`，密码 `root123456`。生产可用环境变量 `MYSQL_URL`、`MYSQL_USER`、`MYSQL_PASSWORD` 覆盖。配置里没有 `ddl-auto`。
- `backend/ruoyi-admin/src/main/resources/application.yml`：Redis 仍是 `localhost:6379`、无密码。上传目录改为 `D:/ProjectFile/sheet-editor/data/upload`。
- `frontend/vite.config.js`：开发端口从官方的 `80` 改为 `5173`，只听 `127.0.0.1`，启动时不自动打开浏览器。`/dev-api` 仍代理到 `http://localhost:8080`。

不做什么：

- 不写表格上传、改列、导出。
- 不改若依的用户、角色、菜单、代码生成。
- 不新建业务表。下面的语句是若依官方脚本，确认前不执行。

## 输入 / 输出

开发库不再用 H2。Redis 用已经在跑的容器 `redis`（6379）。MySQL 用已经在跑的容器 `mysql`（3306）。新建库 `ry-vue`，不使用现有的空库 `app`。

登录、退出、修改密码都走若依原有接口：

- 登录页提交账号密码和验证码，验证码在 Redis。
- 右上角退出，调用 `/logout`。
- 个人中心「修改密码」调用 `/system/user/profile/updatePwd`，要填旧密码和新密码。官方脚本没有邮件重置。

官方初始账号写在 `backend/sql/ry_20260417.sql`：`admin` / `admin123`，测试员 `ry` / `admin123`。本次还没有登录验证。

## 表结构（确认前不执行）

先建库，再按原文件执行，不改脚本内容。两个脚本里都有 `drop table if exists`，只作用于 `ry-vue`。

```sql
CREATE DATABASE IF NOT EXISTS `ry-vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

然后在库 `ry-vue` 中执行：

- `backend/sql/ry_20260417.sql`
- `backend/sql/quartz.sql`

`ry_20260417.sql` 里的表：`sys_dept`、`sys_user`、`sys_post`、`sys_role`、`sys_menu`、`sys_user_role`、`sys_role_menu`、`sys_role_dept`、`sys_user_post`、`sys_oper_log`、`sys_dict_type`、`sys_dict_data`、`sys_config`、`sys_logininfor`、`sys_job`、`sys_job_log`、`sys_notice`、`sys_notice_read`、`gen_table`、`gen_table_column`。

`quartz.sql` 里的表：`QRTZ_JOB_DETAILS`、`QRTZ_TRIGGERS`、`QRTZ_SIMPLE_TRIGGERS`、`QRTZ_CRON_TRIGGERS`、`QRTZ_BLOB_TRIGGERS`、`QRTZ_CALENDARS`、`QRTZ_PAUSED_TRIGGER_GRPS`、`QRTZ_FIRED_TRIGGERS`、`QRTZ_SCHEDULER_STATE`、`QRTZ_LOCKS`、`QRTZ_SIMPROP_TRIGGERS`。

`sys_user` 关键列：`user_id`、`user_name`、`nick_name`、`password`（BCrypt）、`status`、`pwd_update_date`。

## 验收

确认并执行上面的语句之后，才算底座可用：

1. 在 `backend` 执行 `mvn -DskipTests package`，生成 `backend/ruoyi-admin/target/ruoyi-admin.jar`。
2. `java -jar backend/ruoyi-admin/target/ruoyi-admin.jar` 监听 8080，日志里没有数据源连接失败。
3. 在 `frontend` 执行 `npm install`，再 `npm run dev`。页面 `http://127.0.0.1:5173/` 出现若依登录页。
4. 用 `admin` / `admin123` 加上页面上的验证码登录，进入首页。右上角退出后，再访问首页会回到登录页。
5. 再次登录，在个人中心修改密码。用新密码能登录，旧密码不能登录。
