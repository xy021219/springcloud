package com.ruoyi.web.core.seckill;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.exception.ServiceException;

@Component
public class SeckillStock
{
    public static final String REQUEST_PREFIX = "seckill:req:";

    public static String stockKey(String productCode)
    {
        return "seckill:stock:" + productCode;
    }

    public static String buyersKey(String productCode)
    {
        return "seckill:buyers:" + productCode;
    }

    public static String buyerLabel(long userId)
    {
        return "\u7528\u6237" + userId;
    }

    static final String BUY_LUA = ""
            + "local bought = redis.call('SISMEMBER', KEYS[2], ARGV[1])\n"
            + "if bought == 1 then return -2 end\n"
            + "local stock = tonumber(redis.call('GET', KEYS[1]))\n"
            + "if stock == nil or stock <= 0 then return -1 end\n"
            + "local left = redis.call('DECR', KEYS[1])\n"
            + "redis.call('SADD', KEYS[2], ARGV[1])\n"
            + "return left\n";

    static final String RESTORE_LUA = ""
            + "local removed = redis.call('SREM', KEYS[2], ARGV[1])\n"
            + "if removed == 1 then redis.call('INCR', KEYS[1]) end\n"
            + "return removed\n";

    private final StringRedisTemplate redis;

    private final DefaultRedisScript<Long> buyScript;

    private final DefaultRedisScript<Long> restoreScript;

    public SeckillStock(StringRedisTemplate redis)
    {
        this.redis = redis;
        this.buyScript = script(BUY_LUA);
        this.restoreScript = script(RESTORE_LUA);
    }

    public boolean unmark(String productCode, long userId)
    {
        Long removed = redis.opsForSet().remove(buyersKey(productCode), buyerLabel(userId));
        return removed != null && removed.longValue() > 0;
    }

    public int addOne(String productCode)
    {
        int next = SeckillSale.returnedOne(current(productCode));
        put(productCode, next);
        return next;
    }

    public void ensure(String productCode, int mysqlStock, Collection<Long> boughtUserIds)
    {
        redis.opsForValue().setIfAbsent(stockKey(productCode), String.valueOf(mysqlStock));
        markBought(productCode, boughtUserIds);
    }

    public void markBought(String productCode, Collection<Long> boughtUserIds)
    {
        if (boughtUserIds == null)
        {
            return;
        }
        for (Long userId : boughtUserIds)
        {
            if (userId != null)
            {
                redis.opsForSet().add(buyersKey(productCode), buyerLabel(userId));
            }
        }
    }

    public int current(String productCode)
    {
        String raw = redis.opsForValue().get(stockKey(productCode));
        if (raw == null)
        {
            return 0;
        }
        return Integer.parseInt(raw);
    }

    public void put(String productCode, int redisStock)
    {
        redis.opsForValue().set(stockKey(productCode), String.valueOf(redisStock));
    }

    public void clearBuyers(String productCode)
    {
        redis.delete(buyersKey(productCode));
    }

    public int tryBuy(String productCode, long userId)
    {
        Long result = redis.execute(buyScript, List.of(stockKey(productCode), buyersKey(productCode)), buyerLabel(userId));
        if (result == null || result.longValue() == -1L)
        {
            throw new ServiceException(SeckillSale.SOLD_OUT);
        }
        if (result.longValue() == -2L)
        {
            throw new ServiceException(SeckillSale.ALREADY);
        }
        return result.intValue();
    }

    public void restore(String productCode, long userId)
    {
        redis.execute(restoreScript, List.of(stockKey(productCode), buyersKey(productCode)), buyerLabel(userId));
    }

    public void reset(String productCode, int redisStock)
    {
        put(productCode, redisStock);
        clearBuyers(productCode);
        Set<String> requests = redis.keys(REQUEST_PREFIX + "*");
        if (requests != null && !requests.isEmpty())
        {
            redis.delete(requests);
        }
    }

    public String cached(String requestId)
    {
        return redis.opsForValue().get(REQUEST_PREFIX + requestId);
    }

    public boolean claim(String requestId)
    {
        Boolean first = redis.opsForValue().setIfAbsent(REQUEST_PREFIX + requestId, "PENDING", Duration.ofMinutes(30));
        return Boolean.TRUE.equals(first);
    }

    public void remember(String requestId, String value)
    {
        redis.opsForValue().set(REQUEST_PREFIX + requestId, value);
    }

    public void forget(String requestId)
    {
        redis.delete(REQUEST_PREFIX + requestId);
    }

    private static DefaultRedisScript<Long> script(String text)
    {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(text);
        return script;
    }
}
