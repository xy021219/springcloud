package com.ruoyi.web.core.seckill;

public class SeckillActivity
{
    private long activityId;

    private String name;

    private int stock;

    private String productCode = "";

    private String productName = "";

    public long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(long activityId)
    {
        this.activityId = activityId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getStock()
    {
        return stock;
    }

    public void setStock(int stock)
    {
        this.stock = stock;
    }

    public String getProductCode()
    {
        return productCode;
    }

    public void setProductCode(String productCode)
    {
        this.productCode = productCode;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }
}
