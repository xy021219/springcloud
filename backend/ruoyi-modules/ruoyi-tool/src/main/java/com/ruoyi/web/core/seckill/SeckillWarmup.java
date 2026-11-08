package com.ruoyi.web.core.seckill;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SeckillWarmup implements ApplicationRunner
{
    private final SeckillService seckillService;

    public SeckillWarmup(SeckillService seckillService)
    {
        this.seckillService = seckillService;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        seckillService.activity();
    }
}
