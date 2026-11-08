package com.ruoyi.web.core.seckill;

public class SeckillStockForm
{
    private int redisStock;

    private int dbStock;

    private String redisMode;

    private String dbMode;

    public int getRedisStock()
    {
        return redisStock;
    }

    public void setRedisStock(int redisStock)
    {
        this.redisStock = redisStock;
    }

    public int getDbStock()
    {
        return dbStock;
    }

    public void setDbStock(int dbStock)
    {
        this.dbStock = dbStock;
    }

    public String getRedisMode()
    {
        return redisMode;
    }

    public void setRedisMode(String redisMode)
    {
        this.redisMode = redisMode;
    }

    public String getDbMode()
    {
        return dbMode;
    }

    public void setDbMode(String dbMode)
    {
        this.dbMode = dbMode;
    }
}
