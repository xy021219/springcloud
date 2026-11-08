package com.ruoyi.web.core.seckill;

public class SeckillOrderRow
{
    private String orderNo = "";

    private String buyer = "";

    private String productCode = "";

    private String productName = "";

    private String createTime = "";

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getBuyer()
    {
        return buyer;
    }

    public void setBuyer(String buyer)
    {
        this.buyer = buyer;
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

    public String getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(String createTime)
    {
        this.createTime = createTime;
    }
}
