# 项目注意点

只记这个仓库里会反复踩的事。开工先看这一份。

新碰到只属于本仓库、下次还要避开的事，追加在文末，写清文件路径和当时的现象。不要只留在当次对话里。只发生一次、换个项目就不会碰到的，写进当天的 `docs/changes/`，不要堆到这里。

## 验收前先换上这次的进程

改过某个 Java 服务、要用页面或接口看结果之前，先做完这三步，再发请求：

1. 停掉该服务正在监听的进程。改表和报表是 `9204`，网关 `8080`，认证 `9200`，系统 `9201`。
2. 用本机 `D:\Java\Maven\apache-maven-3.10.0\bin\mvn.cmd` 打包。`-pl` 必须带 `-am`。PowerShell 里 `-D` 参数加引号。jar 还被旧进程占用时，`repackage` 改不了文件名，所以先停再打包。
3. 启动新 jar，在日志里看到 `Started`，并且这条日志的时间晚于这次打包，然后再测。

不要先请求端口上已经在跑的进程，发现还是旧行为，再停掉重来。这件事只该发生过一次。

前端只改了 `.vue` 时，Vite 会自己更新，不用为了看页面去重启 Java。没改到的服务不要重启。

新菜单写进 `sys_menu` 之后，已经登录的浏览器要退出再登录，路由才会带上新页面。不重新登录就打开新地址，会看到 404。这不是服务没重启。

## 看着没用也不要改

下面这些文件或配置，有的行当前跑不起来，有的模块页面用不到。不要为了收拾它们去改、去删、去重跑。

- `backend/ruoyi-gateway/src/main/resources/bootstrap.yml`、`backend/ruoyi-auth/src/main/resources/bootstrap.yml`、`backend/ruoyi-modules/ruoyi-system/src/main/resources/bootstrap.yml`、`backend/ruoyi-modules/ruoyi-tool/src/main/resources/bootstrap.yml` 里的 `optional:nacos`。启动日志会出现 `MalformedInputException`，是因为 Nacos 里带中文的配置解析失败。进程已经 `Started` 就可以继续。不要改成必选，也不要删掉这几行。真正生效的是各服务自己的 `application.yml`。
- `backend/sql/ry_20260417.sql`、`backend/sql/quartz.sql`、`backend/sql/ry_config_20260918.sql`、`backend/sql/ry_seata_20210128.sql`。这些脚本里有删表语句，库已经导入过。不要改脚本，不要再执行。业务上的增量写成新的 sql 文件。库 `ry-vue` 留着，不要对它建表、改表、导数据。业务库是 `ry-cloud`，Nacos 库是 `ry-config`。
- 代码生成、定时任务、文件、监控、Sentinel、Seata 的模块还在仓库里。不要启动，也不要因为当前功能用不到就删模块或改它们的配置。
- `backend/ruoyi-modules/ruoyi-tool/src/main/java/com/ruoyi/web/core/sheet/SheetJobs.java` 里的同时 2 份限制。做秒杀、报表或其他页面时不要改这个信号量。
- Docker：`C:\Users\Administrator\.docker\daemon.json` 的镜像加速，以及 Docker Desktop 设置里的 `ProxyHTTPMode=system`、代理留空、`UseContainerdSnapshotter=false`。这些值看起来像空着或没开，改了之后引擎起不来，或者从镜像站拉层会失败。细节在 `docs/local-env.md`。
- `backend/LICENSE`、`frontend/LICENSE`。登录、退出、修改密码继续用若依自带的页面和接口。
- 不要加 `ddl-auto: update`。表结构要变时，先给出语句，停下等确认。

## 这个仓库里已经对过的做法

- 中文的规格和变更记录要用 UTF-8 保存。在 Windows 上用会把中文存成 GBK 的写法写 `docs/`，下次打开就是乱码。用 node 按 utf8 写入。
- Maven 只跑一个模块的测试时，加上 `-Dsurefire.failIfNoSpecifiedTests=false`，否则没有对上测试名的模块会把构建打失败。
- 网关签名只处理路径恰好是 `POST /tool/seckill/order` 的请求。新的秒杀接口不要放在这个路径上，否则会被要求签名。
- 秒杀在 Redis 里记下的购买人是「用户」加编号，例如 `用户1`。集合里如果还留着纯数字 `1`，挡不住 `用户1`。更换库存键或购买人写法时，把已有订单里的用户按新写法补进集合。现在的库存键是 `seckill:stock:P1010`，购买人键是 `seckill:buyers:P1010`。
- 消费者遇到订单唯一键冲突时，确认消息并丢掉，不要再减数据库库存，也不要把 Redis 库存加回去。重复投递不能把库存还回去。
- 并发测试页的「重置」会删掉当前商品在这个活动上的订单，Redis 库存不动，数据库库存加上这些订单的数量，并清掉 Redis 里的购买人。确认弹窗里的已购数量就是要加回数据库的数量。验收别的功能时不要在弹窗里点确定。
- 「确认修改库存」是在现有 Redis 和数据库库存上增加或减少。先算出结果，Redis 不能高于数据库，也不能小于 0，通过之后才写入。输入框不是最终库存。执行测试不用这两个输入框。
- 秒杀页和并发测试共用 Redis 键 seckill:stock:商品代码。秒杀页打开时只读这个键，不要在打开页面时用数据库库存去写入 Redis。
- 秒杀页的取消购买只删当前登录用户在该商品上的订单，并从 Redis 购买人集合 seckill:buyers:商品代码 里拿掉「用户」加编号。有订单或有这条购买人时，Redis 库存和数据库库存各加 1。不要再把全部订单删掉或把库存改回 10。
- MySQL 容器的 `NOW()` 走的是 UTC，比这台机器的本地时间晚 8 小时。页面上的下单时间先对照 `SELECT NOW()`，不要直接改成另一套时区。

- 侧边栏里用不到的菜单是把 `sys_menu.visible` 改成 `1`，语句在 `backend/sql/hide_unused_menus.sql`。编号是 2、4、102 到 108、109 到 116、500、501。`frontend/src/views/system/role/index.vue` 里的 `hiddenMenuIds` 跟这组编号一致，角色页的菜单树才不会再列出它们。不要靠重跑官方 SQL 把这些菜单重新显示出来，也不要删掉对应的 Vue 文件。角色页不再让人填权限字符，新建角色时页面自己生成 `roleKey`。
