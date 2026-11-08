package com.ruoyi.web.core.seckill;

public class SeckillProduct
{
    private String code = "";

    private String name = "";

    private int dbStock;

    private int redisStock;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getDbStock()
    {
        return dbStock;
    }

    public void setDbStock(int dbStock)
    {
        this.dbStock = dbStock;
    }

    public int getRedisStock()
    {
        return redisStock;
    }

    public void setRedisStock(int redisStock)
    {
        this.redisStock = redisStock;
    }
}
