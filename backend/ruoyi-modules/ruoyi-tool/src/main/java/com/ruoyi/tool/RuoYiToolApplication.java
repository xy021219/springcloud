package com.ruoyi.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication(scanBasePackages = { "com.ruoyi.tool", "com.ruoyi.web" })
public class RuoYiToolApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiToolApplication.class, args);
    }
}
