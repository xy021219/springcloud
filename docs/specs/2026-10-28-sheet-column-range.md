## 目标

改表从「恰好两列」改为「1 到 3 列」，导出的表头能看出哪些列被改过。报表里的对应事项与这条规则一致。

## 场景

改表和开发报表已经按原规格做好。业务试用后提出：有的表只改备注一列，有的要同时改姓名、部门和备注，恰好两列做不了；导出的文件也看不出改过哪几列。

## 过程

本规格点头之前，不改业务代码，不改菜单，不改已有规格正文。

复用检索跳过：列数和表头标注是本业务规则。读写 `.xlsx` 仍用仓库里已有的 Apache POI，不另引库。

点头之后按下面顺序做，不能先改实现再补检查：

1. `backend/` 各 `pom.xml` 里没有 `junit`，也没有 `spring-boot-starter-test`。只给 `ruoyi-admin` 增加 `spring-boot-starter-test`，版本跟随父工程的 Spring Boot 4.1.0，不另选测试框架。
2. 先写 `SheetWorkbook` 的失败测试，跑一次，确认因「仍要求恰好两列」而失败。
3. 再改实现，使同一测试通过。
4. 然后改页面文案和报表模拟事项。
5. 收尾写 `docs/changes/2026-10-28.md`。同时把 `docs/specs/2026-10-27-upload-edit-export.md` 里「恰好两列」「`editedColumns` 恰好两个」改成指向本规格。

## 范围

改这些文件：

- `backend/ruoyi-admin/pom.xml`：只加测试依赖
- `backend/ruoyi-admin/src/main/java/com/ruoyi/web/core/sheet/SheetWorkbook.java`：列数校验、写出表头
- `backend/ruoyi-admin/src/test/java/` 下新增 `SheetWorkbook` 的测试
- `frontend/src/views/tool/sheet/index.vue`：勾选上限和提示
- `backend/ruoyi-admin/src/main/java/com/ruoyi/web/core/report/AiReport.java`：一条事项文案

不做什么：

- 不建业务表，不写 DDL，上传内容仍不入库
- 不改登录、权限、菜单
- 不改预览接口的返回结构，不改导出文件名 `edited.xlsx`
- 不改 5 MB、500 行、50 列的限制
- 不改未勾选列的单元格：仍用预览时的原文
- 不调用外部 AI，不改报表其余模拟数字

## 输入 / 输出

页面 `http://127.0.0.1:5173/tool/sheet`：

- 勾选 1 列、2 列或 3 列时，只有这些列能改文字，导出按钮可用。
- 未勾选时导出按钮不可用。
- 已勾选 3 列再勾第 4 列时，提示「最多修改 3 列」，已选列不变。
- 取消某一列时，该列恢复成预览原文。
- 页面上的表头保持上传时的原文。说明文字改为：勾选 1 到 3 列后才能改文字。

导出请求仍是 `POST /tool/sheet/export`，JSON 字段不变：`headers`、`rows`、`editedColumns`。`headers` 仍是原表头，由后端在写出时追加标注。

`editedColumns` 去重后须为 1 到 3 个，且都落在表头下标内。否则接口返回「请选择 1 到 3 列」。

导出的 `.xlsx`：被勾选列的表头是原文字加「（已改）」，例如「备注」写成「备注（已改）」。未勾选列表头不变。被勾选列的单元格用页面上改后的文字，其余列用预览原文。

报表 `GET /tool/report/overview`：事项「上传表格并改两列」改为「上传表格并改 1 到 3 列」，负责人、状态、日期仍是 AI、已完成、10-26。模块「改表」仍是进度 100、状态已完成。`cards` 第一项仍是「已完成需求」、数值 4。

## 验收

1. 点头后、改 `SheetWorkbook` 之前，先跑新增测试，结果为失败。
2. 改完后，在 `backend/` 执行 `mvn -pl ruoyi-admin -am test -Dtest=SheetWorkbookTest`，通过。测试至少覆盖：勾选 1 列能导出且该列表头带「（已改）」；勾选 3 列能导出；勾选 0 列或 4 列时抛出「请选择 1 到 3 列」；未勾选列的单元格与输入原文相同。
3. `mvn -pl ruoyi-admin -am -DskipTests package` 结果为 BUILD SUCCESS。
4. 登录后打开改表页：3 列表上传后可只勾 1 列并导出；勾满 3 列后再勾第 4 列会看到「最多修改 3 列」。导出文件里勾选列表头带「（已改）」，未勾选列的文字与上传时相同。
5. 打开 http://127.0.0.1:5173/tool/report ，事项表中有「上传表格并改 1 到 3 列」。接口 `cards` 第一项仍是「已完成需求」、数值 4。
