package com.ruoyi.web.core.seckill;

import java.util.ArrayList;
import java.util.List;

public class SeckillLoadView
{
    private String redisStock = "0";

    private String dbStock = "0";

    private String productCode = "";

    private String productName = "";

    private String summary = "";

    private List<String> buyers = new ArrayList<>();

    private String insightUrl = "";

    private int orderCount;

    public String getRedisStock()
    {
        return redisStock;
    }

    public void setRedisStock(String redisStock)
    {
        this.redisStock = redisStock;
    }

    public String getDbStock()
    {
        return dbStock;
    }

    public void setDbStock(String dbStock)
    {
        this.dbStock = dbStock;
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

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public List<String> getBuyers()
    {
        return buyers;
    }

    public void setBuyers(List<String> buyers)
    {
        this.buyers = buyers;
    }

    public String getInsightUrl()
    {
        return insightUrl;
    }

    public void setInsightUrl(String insightUrl)
    {
        this.insightUrl = insightUrl;
    }

    public int getOrderCount()
    {
        return orderCount;
    }

    public void setOrderCount(int orderCount)
    {
        this.orderCount = orderCount;
    }
}
