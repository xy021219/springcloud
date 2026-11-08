## 目标

在系统工具里打开并发测试页，点一次按钮，用 200 个用户同时抢 50 件。页面上同时看到执行前和执行后的 Redis 结果。购买人显示成用户1、用户2。可以把这组测试键重置掉。

## 范围

- 新增页面 `frontend/src/views/tool/loadtest/index.vue` 和接口 `GET /tool/seckill/load`、`POST /tool/seckill/load/run`、`POST /tool/seckill/load/reset`
- 购买人写入 Redis 时用「用户」加编号
- 页面带 RedisInsight 链接 `http://127.0.0.1:5540/`
- 菜单数据：`sys_menu` 121，权限沿用 `tool:seckill:list`
- 不改秒杀页库存 `seckill:stock:1`，不改表结构，不改改表的同时 2 份限制
- 签名仍然只拦 `POST /tool/seckill/order`

## 输入 / 输出

- 点「执行测试」：先读出当前的 `seckill:load:stock`、`seckill:load:buyers`、`seckill:load:summary`，再把库存设成 50，200 个线程同时走抢购用的 Lua
- 返回执行前、执行后两份：剩余库存、汇总、购买人列表
- 点「重置」：删掉上面三个键，页面显示「已重置」

## 验收

- 打开系统工具下的并发测试
- 执行后，执行后一栏剩余库存是 0，购买人 50 个，名称是用户加编号，汇总里 success=50、soldOut=150
- 执行前一栏是点按钮那一刻的 Redis
- 重置后三个测试键消失，秒杀页库存不变
