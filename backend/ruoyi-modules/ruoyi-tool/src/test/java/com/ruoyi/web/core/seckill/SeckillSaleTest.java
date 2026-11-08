package com.ruoyi.web.core.seckill;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import com.ruoyi.common.core.exception.ServiceException;

class SeckillSaleTest
{
    private static final String SOLD_OUT = "\u5df2\u7ecf\u5356\u5b8c";

    private static final String ALREADY = "\u5df2\u7ecf\u62a2\u8fc7";

    @Test
    void soldOutWhenStockIsZero()
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> SeckillSale.remainingAfterBuy(0, false));
        assertEquals(SOLD_OUT, ex.getMessage());
    }

    @Test
    void alreadyBoughtComesBeforeStock()
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> SeckillSale.remainingAfterBuy(3, true));
        assertEquals(ALREADY, ex.getMessage());
    }

    @Test
    void buyDecreasesStockByOne()
    {
        assertEquals(1, SeckillSale.remainingAfterBuy(2, false));
    }

    @Test
    void resetRestoresTen()
    {
        assertEquals(10, SeckillSale.stockAfterReset());
    }

    @Test
    void redisStockCannotExceedDatabase()
    {
        ServiceException ex = assertThrows(ServiceException.class, () -> SeckillSale.limit(80, 50));
        assertEquals("Redis\u5e93\u5b58\u4e0d\u80fd\u9ad8\u4e8e\u6570\u636e\u5e93\u5e93\u5b58", ex.getMessage());
    }

    @Test
    void redisStockMayEqualDatabase()
    {
        SeckillSale.limit(50, 50);
    }

    @Test
    void increaseAddsToCurrentStock()
    {
        assertEquals(15, SeckillSale.changed(5, 10, true));
        assertArrayEquals(new int[] { 10, 15 }, SeckillSale.nextStocks(0, 5, 10, 10, true, true));
    }

    @Test
    void decreaseSubtractsFromCurrentStock()
    {
        assertEquals(3, SeckillSale.changed(5, 2, false));
        assertArrayEquals(new int[] { 1, 9 }, SeckillSale.nextStocks(2, 9, 1, 0, false, true));
    }

    @Test
    void stockChangeRejectsNegativeAndRedisAboveDatabase()
    {
        ServiceException negative = assertThrows(ServiceException.class,
                () -> SeckillSale.nextStocks(2, 5, 3, 0, false, true));
        assertEquals("\u5e93\u5b58\u4e0d\u80fd\u5c0f\u4e8e0", negative.getMessage());
        ServiceException over = assertThrows(ServiceException.class,
                () -> SeckillSale.nextStocks(0, 5, 10, 1, true, true));
        assertEquals("Redis\u5e93\u5b58\u4e0d\u80fd\u9ad8\u4e8e\u6570\u636e\u5e93\u5e93\u5b58", over.getMessage());
    }

    @Test
    void resetReturnsSoldCountToDatabaseOnly()
    {
        assertEquals(20, SeckillSale.returnedDatabase(5, 15));
    }

    @Test
    void cancelReturnsOneEachWhenAPurchaseExists()
    {
        assertEquals(11, SeckillSale.returnedOne(10));
        assertEquals(true, SeckillSale.shouldReturnOne(true, false));
        assertEquals(true, SeckillSale.shouldReturnOne(false, true));
        assertEquals(false, SeckillSale.shouldReturnOne(false, false));
    }

    @Test
    void zeroRedisStockReadsAsSoldOut()
    {
        assertEquals("\u5df2\u552e\u7f44", SeckillSale.stockText(0));
        assertEquals("10", SeckillSale.stockText(10));
        assertEquals("\u5df2\u552e\u5b8c", SeckillSale.failureText("\u5df2\u7ecf\u5356\u5b8c"));
    }

    @Test
    void waveSummaryUsesChineseLabels()
    {
        String summary = SeckillSale.waveSummary(2, 198, 0, "0", 2, 33);
        assertEquals("\u5df2\u552e=2 \u672a\u8d2d\u7528\u6237=198 \u5df2\u62a2\u8fc7=0 Redis\u5269\u4f59\u5e93\u5b58=0 \u5df2\u8d2d\u7528\u6237=2 \u6b64\u6b21\u62a2\u8d2d\u65f6\u957f=33\u6beb\u79d2",
                summary);
        assertEquals(summary, SeckillSale.displaySummary(
                "success=2 soldOut=198 already=0 left=0 buyers=2 ms=33"));
    }
}
