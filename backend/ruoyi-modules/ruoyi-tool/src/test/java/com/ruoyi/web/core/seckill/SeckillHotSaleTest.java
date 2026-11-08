package com.ruoyi.web.core.seckill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.ruoyi.common.core.exception.ServiceException;

class SeckillHotSaleTest
{
    private static final String SOLD_OUT = "\u5df2\u7ecf\u5356\u5b8c";

    private static final String ALREADY = "\u5df2\u7ecf\u62a2\u8fc7";

    @Test
    void secondOrderFailsWhenStockIsOne()
    {
        SeckillHotSale sale = new SeckillHotSale();
        sale.preload(1);
        sale.buy(1L, "first");
        ServiceException ex = assertThrows(ServiceException.class, () -> sale.buy(2L, "second"));
        assertTrue(SOLD_OUT.equals(ex.getMessage()) || ALREADY.equals(ex.getMessage()));
    }

    @Test
    void sameRequestIdDecreasesStockOnce()
    {
        SeckillHotSale sale = new SeckillHotSale();
        sale.preload(10);
        int first = sale.buy(1L, "same");
        int second = sale.buy(1L, "same");
        assertEquals(first, second);
        assertEquals(9, sale.stock());
    }

    @Test
    void sameUserCannotBuyTwice()
    {
        SeckillHotSale sale = new SeckillHotSale();
        sale.preload(5);
        sale.buy(1L, "a");
        ServiceException ex = assertThrows(ServiceException.class, () -> sale.buy(1L, "b"));
        assertEquals(ALREADY, ex.getMessage());
        assertEquals(4, sale.stock());
    }
}
