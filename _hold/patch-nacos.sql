USE `ry-config`;
UPDATE config_info
SET content = REPLACE(content, 'password: password', 'password: root123456')
WHERE content LIKE '%jdbc:mysql://localhost:3306/ry-cloud%';
UPDATE config_info SET content = REPLACE(content, '            # 文件服务
            - id: ruoyi-file', '            - id: ruoyi-tool
              uri: lb://ruoyi-tool
              predicates:
                - Path=/tool/**
            # 文件服务
            - id: ruoyi-file')
WHERE data_id = 'ruoyi-gateway-dev.yml' AND content NOT LIKE '%id: ruoyi-tool%';
UPDATE config_info SET content = REPLACE(content, '      - /system/notice
', '      - /system/notice
      - /tool/sheet/export
')
WHERE data_id = 'ruoyi-gateway-dev.yml' AND content NOT LIKE '%/tool/sheet/export%';
UPDATE config_info SET content = REPLACE(content, 'spring:
  data:
    redis:', 'spring:
  codec:
    max-in-memory-size: 10MB
  data:
    redis:')
WHERE data_id = 'ruoyi-gateway-dev.yml' AND content NOT LIKE '%max-in-memory-size%';
UPDATE config_info SET md5 = MD5(content) WHERE data_id IN ('ruoyi-gateway-dev.yml','ruoyi-system-dev.yml','ruoyi-gen-dev.yml','ruoyi-job-dev.yml');
INSERT INTO config_info (data_id, group_id, content, md5, src_user, src_ip, app_name, tenant_id, c_desc, type, encrypted_data_key)
SELECT 'ruoyi-tool-dev.yml', 'DEFAULT_GROUP', 'spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
  data:
    redis:
      host: localhost
      port: 6379
      password: ', MD5('spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
  data:
    redis:
      host: localhost
      port: 6379
      password: '), 'local', '127.0.0.1', '', 'public', 'tool', 'yaml', ''
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM config_info WHERE data_id = 'ruoyi-tool-dev.yml' AND group_id = 'DEFAULT_GROUP' AND tenant_id = 'public');