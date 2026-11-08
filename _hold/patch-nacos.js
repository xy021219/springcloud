const fs = require("fs");
function q(s) {
  return "'" + s.replace(/'/g, "''") + "'";
}
const fileRoute = "            # \u6587\u4ef6\u670d\u52a1\n            - id: ruoyi-file";
const toolRoute = [
  "            - id: ruoyi-tool",
  "              uri: lb://ruoyi-tool",
  "              predicates:",
  "                - Path=/tool/**",
  "            # \u6587\u4ef6\u670d\u52a1",
  "            - id: ruoyi-file"
].join("\n");
const notice = "      - /system/notice\n";
const noticePlus = "      - /system/notice\n      - /tool/sheet/export\n";
const springHead = "spring:\n  data:\n    redis:";
const springHeadPlus = "spring:\n  codec:\n    max-in-memory-size: 10MB\n  data:\n    redis:";
const toolYml = [
  "spring:",
  "  servlet:",
  "    multipart:",
  "      max-file-size: 10MB",
  "      max-request-size: 10MB",
  "  data:",
  "    redis:",
  "      host: localhost",
  "      port: 6379",
  "      password: "
].join("\n");
const sql = [
  "USE `ry-config`;",
  "UPDATE config_info",
  "SET content = REPLACE(content, 'password: password', 'password: root123456')",
  "WHERE content LIKE '%jdbc:mysql://localhost:3306/ry-cloud%';",
  "UPDATE config_info SET content = REPLACE(content, " + q(fileRoute) + ", " + q(toolRoute) + ")",
  "WHERE data_id = 'ruoyi-gateway-dev.yml' AND content NOT LIKE '%id: ruoyi-tool%';",
  "UPDATE config_info SET content = REPLACE(content, " + q(notice) + ", " + q(noticePlus) + ")",
  "WHERE data_id = 'ruoyi-gateway-dev.yml' AND content NOT LIKE '%/tool/sheet/export%';",
  "UPDATE config_info SET content = REPLACE(content, " + q(springHead) + ", " + q(springHeadPlus) + ")",
  "WHERE data_id = 'ruoyi-gateway-dev.yml' AND content NOT LIKE '%max-in-memory-size%';",
  "UPDATE config_info SET md5 = MD5(content) WHERE data_id IN ('ruoyi-gateway-dev.yml','ruoyi-system-dev.yml','ruoyi-gen-dev.yml','ruoyi-job-dev.yml');",
  "INSERT INTO config_info (data_id, group_id, content, md5, src_user, src_ip, app_name, tenant_id, c_desc, type, encrypted_data_key)",
  "SELECT 'ruoyi-tool-dev.yml', 'DEFAULT_GROUP', " + q(toolYml) + ", MD5(" + q(toolYml) + "), 'local', '127.0.0.1', '', 'public', 'tool', 'yaml', ''",
  "FROM DUAL",
  "WHERE NOT EXISTS (SELECT 1 FROM config_info WHERE data_id = 'ruoyi-tool-dev.yml' AND group_id = 'DEFAULT_GROUP' AND tenant_id = 'public');"
].join("\n");
fs.writeFileSync("D:/ProjectFile/sheet-editor/_hold/patch-nacos.sql", sql, "utf8");
