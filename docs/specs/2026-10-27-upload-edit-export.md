页面已接到若依：登录后从「系统工具 / 改表」进入，地址 `/tool/sheet`。接口是 `POST /tool/sheet/preview` 和 `POST /tool/sheet/export`，沿用若依登录。不建业务表。

## 目标

在一个页面上上传一份 `.xlsx`，改其中 1 到 3 列的文字，再下载改后的 `.xlsx`。列数规则以 `docs/specs/2026-10-28-sheet-column-range.md` 为准。

## 范围

新增：

- `backend/`：Java 17、Spring Boot 4.1.2、Maven。本机 `mvn` 构建
- `frontend/`：Vue 3、Vite。一个页面
- `.gitignore`：忽略 `backend/target/`、`frontend/node_modules/`、`frontend/dist/`

不做什么：

- 不建业务表，不写 DDL，上传内容不入库
- 不加登录、权限、微服务，不加第二个页面
- 不加测试技能和评审技能。这一轮不写自动化测试
- 不要求本机连上 MySQL
- 不保留原文件的样式、公式、多工作表。只导出第一个工作表里读到的文字

开发配置使用内存 H2，应用能启动。生产配置单独放：MySQL 地址、用户名、密码只从环境变量读，配置里不写 `ddl-auto: update`。本功能不访问数据库。

解析和导出：本仓库没有同类实现。比较过两个现成库，都是 Apache License 2.0，不搬它们的示例代码。

- [Apache Fesod](https://github.com/apache/fesod)（约 6200 star，孵化中）：按行流式读写，适合大文件。它按模型重建一张新表，这一版要在页面上改任意表头的两列，用不上这套模型。
- Apache POI `poi-ooxml` 6.0.0（2026-10-22 发布，要求 Java 17）：按单元格读写 `.xlsx`。这一版表格不超过 500 行，用它读出文字、再写成新文件。

## 输入 / 输出

输入是用户上传的 `.xlsx`。只读第一个工作表。第 1 行是表头，从第 2 行起是数据。单元格一律按显示文字读取。空表头的列忽略。

限制：文件不超过 5 MB，数据不超过 500 行、50 列。超出、不是 `.xlsx`、没有表头、没有数据行，接口返回 400 和一句中文原因。

页面：

1. 选择文件并上传。
2. 表格显示全部列。用户勾选 1 到 3 列后，只有这些列可以改文字。规则见 `docs/specs/2026-10-28-sheet-column-range.md`。
3. 点导出。未勾选列时按钮不可用。

接口：

- `POST /api/sheets/preview`：表单字段 `file`。返回 `{ "headers": string[], "rows": string[][] }`。
- `POST /api/sheets/export`：JSON `{ "headers": string[], "rows": string[][], "editedColumns": number[] }`。`editedColumns` 为 1 到 3 个列下标，规则见 `docs/specs/2026-10-28-sheet-column-range.md`。响应是 `.xlsx` 附件，文件名 `edited.xlsx`。

导出时，被勾选的列使用页面上改后的文字，其余列使用预览时的原文。后端再核对一次：列数与表头一致，行数不超过 500，列数不超过 50，`editedColumns` 去重后为 1 到 3 项且不越界。不符合则 400。

开发时前端把 `/api` 代理到 `http://localhost:8080`。后端端口 8080。

## 验收

1. 在 `backend/` 执行 `mvn -q -DskipTests package`，构建成功。
2. 在 `frontend/` 执行 `npm run build`，构建成功。
3. 后端和前端都启动后，用一份 3 列、2 行数据的 `.xlsx` 走完页面：只改勾选的列，下载的文件里这些列是新文字，未勾选列与上传时相同。列数以 `docs/specs/2026-10-28-sheet-column-range.md` 为准。
4. 上传一个超过 5 MB 的文件，页面上能看到拒绝原因。
5. 本机不启动 MySQL，上述 1 到 3 仍能完成。
