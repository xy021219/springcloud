## 目标

同一个改表进程里，预览和导出加在一起最多同时处理 2 份表。第 3 个请求马上失败，文案是「当前同时处理的表格太多，请稍后再试」。正在处理的那两份仍各回各的结果。

## 范围

- 新增 `backend/ruoyi-modules/ruoyi-tool/src/main/java/com/ruoyi/web/core/sheet/SheetJobs.java`
- 改 `SheetController`：预览、导出进入处理前先占用名额，结束时释放，包括失败
- 新增 `SheetJobsTest`
- 不改列数规则、文件大小、页面和菜单
- 不引入新库，不用 Sentinel，不把请求放进队列里等待
- 不新开页面

跳过向外搜索现成限流库：只在这一处用 Java 自带的信号量限制同时数量，大约三十行，不新增通用框架。

## 输入 / 输出

- 输入仍是现有的 `POST /tool/sheet/preview` 和 `POST /tool/sheet/export`
- 名额够：返回和现在一样
- 名额不够：接口返回错误，消息为「当前同时处理的表格太多，请稍后再试」。不写出半份 Excel
- 两个人同时上传两份不同的表时，各自拿到自己那份的预览或文件

## 验收

1. `mvn -pl ruoyi-modules/ruoyi-tool -am test -Dtest=SheetJobsTest`：先失败，原因是还没有同时 2 份的上限。再改实现后通过。失败若是编译错误，先修好，不算这次红灯。
2. 测试里同时占满 2 个名额时，第 3 次进入得到上述文案。释放 1 个名额后，下一次进入成功。
3. 浏览器里两个人同时上传未实测。页面上若弹出该文案，变更记录另写。
