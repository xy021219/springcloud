package com.ruoyi.web.core.seckill;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeckillAmqpConfig
{
    public static final String QUEUE = "seckill.order";

    @Bean
    public Queue seckillOrderQueue()
    {
        return new Queue(QUEUE, true);
    }
}
