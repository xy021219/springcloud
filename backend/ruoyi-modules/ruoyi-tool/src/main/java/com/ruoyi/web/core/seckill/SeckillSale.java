package com.ruoyi.web.core.seckill;

import com.ruoyi.common.core.exception.ServiceException;

public final class SeckillSale
{
    public static final String SOLD_OUT = "\u5df2\u7ecf\u5356\u5b8c";

    public static final String ALREADY = "\u5df2\u7ecf\u62a2\u8fc7";

    public static final String SUCCESS = "\u62a2\u5230\u4e86";

    public static final String RESET = "\u5df2\u91cd\u7f6e";

    public static final String CANCELLED = "\u5df2\u53d6\u6d88\u8ba2\u5355";

    public static final String NOTHING = "\u6ca1\u6709\u53ef\u53d6\u6d88\u7684\u8ba2\u5355";

    public static final String RETRY = "\u8bf7\u518d\u8bd5\u4e00\u6b21";

    private SeckillSale()
    {
    }

    public static int remainingAfterBuy(int stock, boolean alreadyBought)
    {
        if (alreadyBought)
        {
            throw new ServiceException(ALREADY);
        }
        if (stock <= 0)
        {
            throw new ServiceException(SOLD_OUT);
        }
        return stock - 1;
    }

    public static int stockAfterReset()
    {
        return 10;
    }

    public static int returnedOne(int stock)
    {
        return stock + 1;
    }

    public static boolean shouldReturnOne(boolean hadOrder, boolean hadMark)
    {
        return hadOrder || hadMark;
    }

    public static final String STOCK_LIMIT = "Redis\u5e93\u5b58\u4e0d\u80fd\u9ad8\u4e8e\u6570\u636e\u5e93\u5e93\u5b58";

    public static void limit(int redisStock, int dbStock)
    {
        if (redisStock > dbStock)
        {
            throw new ServiceException(STOCK_LIMIT);
        }
    }

    public static final String STOCK_NEGATIVE = "\u5e93\u5b58\u4e0d\u80fd\u5c0f\u4e8e0";

    public static final String GONE = "\u5df2\u552e\u5b8c";

    public static final String EMPTY = "\u5df2\u552e\u7f44";

    public static String stockText(int stock)
    {
        if (stock <= 0)
        {
            return EMPTY;
        }
        return String.valueOf(stock);
    }

    public static String failureText(String message)
    {
        if (SOLD_OUT.equals(message))
        {
            return GONE;
        }
        return message == null ? "" : message;
    }

    public static int changed(int current, int amount, boolean increase)
    {
        if (amount < 0)
        {
            throw new ServiceException(STOCK_NEGATIVE);
        }
        return increase ? current + amount : current - amount;
    }

    public static int returnedDatabase(int dbStock, int sold)
    {
        if (sold < 0)
        {
            throw new ServiceException(STOCK_NEGATIVE);
        }
        return dbStock + sold;
    }

    public static int[] nextStocks(int redisStock, int dbStock, int redisAmount, int dbAmount,
            boolean redisIncrease, boolean dbIncrease)
    {
        int nextRedis = changed(redisStock, redisAmount, redisIncrease);
        int nextDb = changed(dbStock, dbAmount, dbIncrease);
        if (nextRedis < 0 || nextDb < 0)
        {
            throw new ServiceException(STOCK_NEGATIVE);
        }
        limit(nextRedis, nextDb);
        return new int[] { nextRedis, nextDb };
    }

    public static String waveSummary(int success, int soldOut, int already, String left, long buyers, long ms)
    {
        String redisLeft = left == null || left.isEmpty() ? "0" : left;
        return "\u5df2\u552e=" + success
                + " \u672a\u8d2d\u7528\u6237=" + soldOut
                + " \u5df2\u62a2\u8fc7=" + already
                + " Redis\u5269\u4f59\u5e93\u5b58=" + redisLeft
                + " \u5df2\u8d2d\u7528\u6237=" + buyers
                + " \u6b64\u6b21\u62a2\u8d2d\u65f6\u957f=" + ms + "\u6beb\u79d2";
    }

    public static String displaySummary(String raw)
    {
        if (raw == null || raw.isEmpty())
        {
            return "";
        }
        if (!raw.startsWith("success="))
        {
            return raw;
        }
        String[] parts = raw.split(" ");
        return waveSummary(
                Integer.parseInt(part(parts, "success")),
                Integer.parseInt(part(parts, "soldOut")),
                Integer.parseInt(part(parts, "already")),
                part(parts, "left"),
                Long.parseLong(part(parts, "buyers")),
                Long.parseLong(part(parts, "ms")));
    }

    private static String part(String[] parts, String key)
    {
        String prefix = key + "=";
        for (String part : parts)
        {
            if (part.startsWith(prefix))
            {
                return part.substring(prefix.length());
            }
        }
        return "0";
    }

    public static String orderNo()
    {
        long value = java.util.concurrent.ThreadLocalRandom.current().nextLong(1_000_000_000_000L);
        return String.format("%012d", value);
    }
}
