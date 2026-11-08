package com.ruoyi.web.core.seckill;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SeckillOrderListener
{
    private final SeckillService seckillService;

    public SeckillOrderListener(SeckillService seckillService)
    {
        this.seckillService = seckillService;
    }

    @RabbitListener(queues = SeckillAmqpConfig.QUEUE)
    public void onOrder(String body)
    {
        String[] parts = body.split(",", -1);
        String productCode = parts.length >= 4 ? parts[3] : "";
        String orderNo = parts.length >= 5 ? parts[4] : "";
        seckillService.persist(Long.parseLong(parts[0]), Long.parseLong(parts[1]), parts[2], productCode, orderNo);
    }
}
