# AGENTS.md —— sheet-editor

> Cursor 每次对话都会读本文件。长步骤放技能，不把流程写进这里。

## 项目

在页面上上传一份表，改 1 到 3 列，再导出。另有一个用模拟数据的开发报表。

底座是若依官方微服务版 RuoYi-Cloud 3.6.8，前端是 RuoYi-Cloud-Vue3（MIT，协议在 `backend/LICENSE` 和 `frontend/LICENSE`）。登录、退出、修改密码用若依自带的，不要再写一套。

- 后端：Java 17、Spring Boot 4.1.0、Spring Cloud。代码在 `backend/`
- 前端：Vue 3、Vite、Element Plus。代码在 `frontend/`，开发端口 5173
- 必须启动：MySQL、Redis、Nacos、网关 8080、认证 9200、系统 9201、改表报表 9204、前端 5173
- 不要启动：代码生成、定时任务、文件、监控、Sentinel、Seata
- 开发库：业务库 `ry-cloud`，Nacos 库 `ry-config`。不要重复导入官方脚本，不要动原来的 `ry-vue`
- 业务配置写在各服务自己的 `application.yml`。Nacos 里带中文的配置解析会报 `MalformedInputException`，引导文件保持 `optional:nacos`，不要改回必选
- 禁止 `ddl-auto: update`
- 构建用本机安装的 `mvn`，不用只靠 Maven Wrapper 代替
- 启动见 `docs/local-env.md`

没有的目录不要猜，先看仓库里实际有什么。

## 沟通

- 一律使用简体中文
- 结论给实测结果。没跑过就写「未实测」，不要编

## 开工

先看 `docs/notes.md`，再看改动有多大。新碰到只属于这个仓库、下次还要避开的事，补进那个文件。

改过某个 Java 服务、要用页面或接口验收之前，先停掉该服务正在跑的进程，打包，再启动，确认日志里的 `Started` 晚于这次打包，然后再测。不要先打旧进程，发现还是旧行为再重启。

- 单文件小改：直接改，跑相关检查，一句话说明
- 新功能：`/spec-plan`，点头后再写代码，收尾 `/change-log`
- 要新引入通用机制（解析、导出、分页、重试这类）时，先走本机技能 `reuse-search`。本仓库已有同类实现，或只是这个表的列名、改写规则，就在本仓库里找

不要在若依之外再加一套登录。规格里没写的页面不要加。表结构要变时，先给出语句，停下等确认。
