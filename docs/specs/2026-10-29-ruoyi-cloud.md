## 目标

把底座从若依单体版换成若依官方微服务版，登录和系统功能用新底座。改表、开发报表两个页面和接口搬过去，规则不变。

## 场景

当前仓库是若依 3.9.2 前后端分离单体版。一个 `ruoyi-admin.jar` 监听 8080，里面同时有登录、改表和报表。官方微服务版是 RuoYi-Cloud master（Spring Boot 4.x、JDK 17、Nacos 3.x），前端用 RuoYi-Cloud-Vue3。本机还没有这套代码，也还没有 Nacos。

## 过程

本规格点头之前：不替换 `backend/`、`frontend/`，不建库，不导入脚本，不下载 Nacos，不停掉正在跑的单体。

点头之后按这个顺序：

1. 停掉当前 8080 上的单体。网关要占用 8080。
2. 新建空库。打开官方脚本，确认里面有删表语句，并且只对新建的空库执行。不把脚本导入现有库 `ry-vue`。
3. 用 Docker 启动 Nacos 单机，配置写进 `ry-config`。不启用 Sentinel、Seata。
4. 拉取官方后端 master 和前端 RuoYi-Cloud-Vue3，协议文件保留。
5. 新增改表和报表服务，只搬这两个功能的代码。
6. 先写失败测试再改这个服务里的实现。样式和页面文案不强制自动测试。
7. 收尾更新 `AGENTS.md`、`docs/local-env.md` 和当天变更记录。

复用检索跳过：底座用你指定的若依官方微服务版，不另找框架。表格仍用 Apache POI，图表仍用前端仓库里的 ECharts。

## 范围

替换：

- `backend/`：换成 RuoYi-Cloud master
- `frontend/`：换成 RuoYi-Cloud-Vue3

新增一个业务服务 `ruoyi-tool`，端口 `9204`，只包含现在的改表和报表：

- `SheetController`、`SheetWorkbook`、`SheetTable`、`SheetExportRequest`、`SheetWorkbookTest`
- `ReportController`、`AiReport`
- 网关把 `/tool/**` 转到这个服务
- 导出接口继续排除 XSS 过滤，避免单元格原文被改写

前端只搬这两页和对应接口文件：

- `frontend/src/views/tool/sheet/index.vue`
- `frontend/src/views/tool/report/index.vue`
- `frontend/src/api/tool/sheet.js`
- `frontend/src/api/tool/report.js`

开发时前端仍用 `http://127.0.0.1:5173/`，请求转到网关 `8080`。

必须同时运行的进程：MySQL、Redis、Nacos、网关 `8080`、认证 `9200`、系统 `9201`、改表报表 `9204`、前端 `5173`。代码生成、定时任务、文件服务、监控不启动。

不做什么：

- 不把改表内容入库，不建业务表
- 不调用外部大模型，报表仍是后端样例
- 不启用 Seata，这两个功能没有跨服务写入
- 不改列数规则：仍是 1 到 3 列，导出表头加「（已改）」
- 不重复执行单体版的 `ry_20260417.sql`、`quartz.sql`
- 不把微服务脚本导入 `ry-vue`

## 数据库

现有库 `ry-vue` 保留，不作为微服务的库。点头后才执行下面两句，字符集与本机 MySQL 的 `utf8mb4` 一致：

```sql
CREATE DATABASE IF NOT EXISTS `ry-cloud` DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS `ry-config` DEFAULT CHARACTER SET utf8mb4;
```

官方脚本在 RuoYi-Cloud master 的 `sql/` 目录，文件名是 `ry_20260402.sql`、`ry_config_20260311.sql`。本机还没有这两份文件。点头后先把脚本开头的删表语句贴出来，确认目标库是上面两个空库，再导入。`ry_seata_20210128.sql` 不导入。

菜单不假设「系统工具」的 `menu_id` 仍是 3。导入 `ry-cloud` 后先查询：

```sql
SELECT menu_id, menu_name, parent_id FROM ry-cloud.sys_menu WHERE menu_name = '系统工具';
SELECT menu_id FROM ry-cloud.sys_menu WHERE menu_id IN (118, 119, 1180, 1181);
```

「系统工具」存在，且 118、119、1180、1181 都空着，才插入改表和开发报表。插入语句沿用 `backend/sql/sheet_menu.sql`、`backend/sql/report_menu.sql`，父级改成查到的「系统工具」编号。列和单体版不一致时，先改语句再给你看，不直接执行。

## 输入 / 输出

页面路径仍是 `/tool/sheet`、`/tool/report`。浏览器只访问前端和网关。

- `POST /tool/sheet/preview`、`POST /tool/sheet/export` 由 `9204` 处理
- `GET /tool/report/overview` 由 `9204` 处理
- 登录、菜单、权限由认证服务和系统服务处理，账号仍是 `admin` / `admin123`

改表和报表的请求体、返回值、错误文案与现在的规格一致。

## 验收

1. `ry-vue` 仍在，表数量不因这次导入变化。`ry-cloud`、`ry-config` 是新建库。
2. 网关、认证、系统、改表报表四个进程都能启动。代码生成等可选进程不启动。
3. 打开 `http://127.0.0.1:5173/`，用 `admin` / `admin123` 登录。菜单里有「改表」和「开发报表」。
4. `mvn -pl ruoyi-modules/ruoyi-tool -am test -Dtest=SheetWorkbookTest` 在改实现前失败，原因是旧规则或测试所在模块尚未接上；改完后通过。失败若是编译错误或数据库没起来，先修好，不算这次红灯。
5. 改表页勾选 1 到 3 列可以导出，第 4 列有提示。导出文件里被改列表头带「（已改）」。这一步若当时没在浏览器走完，变更记录写「未实测」。
6. `GET /tool/report/overview` 经网关访问，`cards` 第一项仍是「已完成需求」、数值 4。
