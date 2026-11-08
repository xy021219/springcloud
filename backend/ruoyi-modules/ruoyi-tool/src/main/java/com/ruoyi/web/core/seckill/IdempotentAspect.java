package com.ruoyi.web.core.seckill;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.exception.ServiceException;

@Aspect
@Component
public class IdempotentAspect
{
    private final SeckillStock stock;

    public IdempotentAspect(SeckillStock stock)
    {
        this.stock = stock;
    }

    @Around("@annotation(com.ruoyi.web.core.seckill.Idempotent)")
    public Object around(ProceedingJoinPoint point) throws Throwable
    {
        String requestId = String.valueOf(point.getArgs()[1]);
        if (!stock.claim(requestId))
        {
            return replay(stock.cached(requestId));
        }
        try
        {
            Object result = point.proceed();
            SeckillActivity activity = (SeckillActivity) result;
            stock.remember(requestId, "OK:" + activity.getStock());
            return result;
        }
        catch (ServiceException ex)
        {
            stock.remember(requestId, "ERR:" + ex.getMessage());
            throw ex;
        }
        catch (RuntimeException ex)
        {
            stock.forget(requestId);
            throw ex;
        }
    }

    private SeckillActivity replay(String cached)
    {
        if (cached == null || "PENDING".equals(cached))
        {
            throw new ServiceException(SeckillSale.RETRY);
        }
        if (cached.startsWith("ERR:"))
        {
            throw new ServiceException(cached.substring(4));
        }
        SeckillActivity activity = new SeckillActivity();
        activity.setActivityId(SeckillService.ACTIVITY_ID);
        activity.setStock(Integer.parseInt(cached.substring(3)));
        return activity;
    }
}
