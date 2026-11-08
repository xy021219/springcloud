# 本机环境（2026-10-29）

底座是若依微服务版 RuoYi-Cloud 3.6.8，前端是 RuoYi-Cloud-Vue3。后端在 `backend/`，前端在 `frontend/`。

业务库是 `ry-cloud`，Nacos 库是 `ry-config`。原来的 `ry-vue` 留着，不要再导入单体脚本，也不要重复导入微服务脚本。登录账号 `admin` / `admin123`。

## 已有软件

| 用途 | 位置 |
| --- | --- |
| JDK 17.0.12 | `D:\Java\JDK\jdk17.0.12`（`JAVA_HOME`） |
| Maven 3.10.0 | `D:\Java\Maven\apache-maven-3.10.0`（`MAVEN_HOME`） |
| Node | v24.14.1，npm 11.11.0 |
| Docker Desktop 4.50.0 | `C:\Program Files\Docker\Docker`（引擎 28.5.1） |

Docker 装在 C 盘，不要再往 D 盘重装一份。`D:\docker` 和 `D:\MySQL` 只放 compose 和数据。

## Docker 引擎配置（不要改坏）

镜像加速写在 `C:\Users\Administrator\.docker\daemon.json`：

```json
"registry-mirrors": ["https://docker.m.daocloud.io"]
```

Docker Desktop 设置（`%APPDATA%\Docker\settings-store.json`）：

- `ProxyHTTPMode` 必须是 `system`。代理留空。不要写成 `disabled`，引擎会起不来。
- 以前手动代理 `http://127.0.0.1:7890` 当时没有程序在听，已经清掉。
- `UseContainerdSnapshotter` 为 `false`（存储驱动 overlay2）。改回 true 后，DaoCloud 拉层会在解压阶段失败。

镜像已经在本机：

- `redis:7`
- `mysql:8.0`（镜像 ID `6cd09145362d`，约 799MB）

层在虚拟磁盘里，不是压缩包，不要解压：

`C:\Users\Administrator\AppData\Local\Docker\wsl\disk\docker_data.vhdx`

## Redis

文件：`D:\docker\docker-compose.yml`  
数据：`D:\docker\redis`  
容器名 `redis`，端口 `6379`，开启 AOF。

## MySQL

文件：`D:\MySQL\docker-compose.yml`  
数据：`D:\MySQL\data`  
容器名 `mysql`，端口 `3306`。

- root 密码：`root123456`
- 已建库：`app`
- 字符集：`utf8mb4` / `utf8mb4_0900_ai_ci`

两个 compose 里都是 `restart: unless-stopped`。若容器被手动停过，Docker 起来后要执行下面的启动命令。

## 启动

按这个顺序。不要启动代码生成、定时任务、文件、监控、Sentinel、Seata。不要用官方 docker-compose 再起一套 MySQL 或 Redis。

1. 打开 Docker Desktop，等到 `docker info` 能打出 Server Version。
2. 启动两个容器：

```powershell
docker compose -f D:\docker\docker-compose.yml up -d
docker compose -f D:\MySQL\docker-compose.yml up -d
```

3. 确认：

```powershell
docker exec redis redis-cli ping
docker exec mysql mysqladmin ping -uroot -proot123456
```

Redis 应返回 `PONG`。MySQL 刚恢复时多等几秒再 ping。

RabbitMQ 用单独的 compose，不要和 MySQL、Redis 放在一起：

```powershell
docker pull docker.m.daocloud.io/library/rabbitmq:3-management
docker tag docker.m.daocloud.io/library/rabbitmq:3-management rabbitmq:3-management
docker compose -f D:\ProjectFile\sheet-editor\backend\docker-compose.rabbitmq.yml up -d --pull never
```

端口 5672，管理界面 15672。账号 `seckill` / `seckill`。

Redis 界面用 RedisInsight，浏览器打开 http://127.0.0.1:5540/ 。容器名 `redisinsight`。连本机 Redis 时主机填 `host.docker.internal`，端口 `6379`，没有密码。

```powershell
docker pull docker.m.daocloud.io/redis/redisinsight:latest
docker tag docker.m.daocloud.io/redis/redisinsight:latest redis/redisinsight:latest
docker run -d --name redisinsight --restart unless-stopped -p 5540:5540 redis/redisinsight:latest
```

4. Nacos 用本机进程，不走 Docker。控制台端口是 `8081`，把 `8080` 留给网关。配置在 `D:\nacos\nacos\conf\application.properties`。

```powershell
$env:JAVA_HOME = 'D:\Java\JDK\jdk17.0.12'
cmd /c "D:\nacos\nacos\bin\startup.cmd -m standalone"
```

等到日志出现 `Nacos started successfully`，并且 `8848`、`8081` 在听。

5. 四个服务各开一个终端。jar 要先打包过。改表服务排除了数据源自动配置，并扫描 `com.ruoyi.web`，否则控制器不会注册。

```powershell
& 'D:\Java\JDK\jdk17.0.12\bin\java.exe' -jar D:\ProjectFile\sheet-editor\backend\ruoyi-auth\target\ruoyi-auth.jar
& 'D:\Java\JDK\jdk17.0.12\bin\java.exe' -jar D:\ProjectFile\sheet-editor\backend\ruoyi-modules\ruoyi-system\target\ruoyi-modules-system.jar
& 'D:\Java\JDK\jdk17.0.12\bin\java.exe' -jar D:\ProjectFile\sheet-editor\backend\ruoyi-gateway\target\ruoyi-gateway.jar
& 'D:\Java\JDK\jdk17.0.12\bin\java.exe' -jar D:\ProjectFile\sheet-editor\backend\ruoyi-modules\ruoyi-tool\target\ruoyi-modules-tool.jar
```

端口：网关 `8080`，认证 `9200`，系统 `9201`，改表和报表 `9204`。

6. 前端：

```powershell
cd D:\ProjectFile\sheet-editor\frontend
npm install
npm run dev
```

打开 `http://127.0.0.1:5173/`。前端把 `/dev-api` 代理到网关 `8080`。

Nacos 里带中文的 yml 解析会报 `MalformedInputException`。各服务的 `bootstrap.yml` 使用 `optional:nacos`，真正生效的是各服务自己的 `application.yml`（Redis、`ry-cloud` 数据源、网关路由）。看到这条错误但进程已经 `Started`，可以继续。

生产数据库连接用环境变量 `MYSQL_URL`、`MYSQL_USER`、`MYSQL_PASSWORD`。配置里没有 `ddl-auto`。

## 收工时停掉

先结束四个 Java 服务、前端，再结束 Nacos（`startup.cmd` 拉起的 `java`）。然后：

```powershell
docker stop mysql redis rabbitmq
```

需要的话再退出 Docker Desktop。
