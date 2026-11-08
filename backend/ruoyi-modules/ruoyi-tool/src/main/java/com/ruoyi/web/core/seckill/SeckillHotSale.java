package com.ruoyi.web.core.seckill;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.ruoyi.common.core.exception.ServiceException;

public final class SeckillHotSale
{
    private int stock;

    private final Set<Long> buyers = new HashSet<>();

    private final Map<String, Outcome> requests = new HashMap<>();

    public void preload(int stock)
    {
        this.stock = stock;
        buyers.clear();
        requests.clear();
    }

    public int stock()
    {
        return stock;
    }

    public int buy(long userId, String requestId)
    {
        Outcome previous = requests.get(requestId);
        if (previous != null)
        {
            if (previous.message != null)
            {
                throw new ServiceException(previous.message);
            }
            return previous.stock;
        }
        try
        {
            int left = take(userId);
            requests.put(requestId, Outcome.ok(left));
            return left;
        }
        catch (ServiceException ex)
        {
            requests.put(requestId, Outcome.error(ex.getMessage()));
            throw ex;
        }
    }

    private int take(long userId)
    {
        if (buyers.contains(userId))
        {
            throw new ServiceException(SeckillSale.ALREADY);
        }
        if (stock <= 0)
        {
            throw new ServiceException(SeckillSale.SOLD_OUT);
        }
        stock = stock - 1;
        buyers.add(userId);
        return stock;
    }

    private static final class Outcome
    {
        private final int stock;

        private final String message;

        private Outcome(int stock, String message)
        {
            this.stock = stock;
            this.message = message;
        }

        private static Outcome ok(int stock)
        {
            return new Outcome(stock, null);
        }

        private static Outcome error(String message)
        {
            return new Outcome(0, message);
        }
    }
}
