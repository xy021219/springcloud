package com.ruoyi.web.core.seckill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

class SeckillConcurrentTest
{
    private static final int STOCK = 50;

    private static final int USERS = 200;

    @Test
    void twoHundredUsersBuyFiftyStockTogether() throws Exception
    {
        String stockKey = "seckill:load:stock";
        String buyersKey = "seckill:load:buyers";
        RedisClient client = RedisClient.create("redis://localhost:6379");
        List<StatefulRedisConnection<String, String>> connections = new ArrayList<>();
        try
        {
            RedisCommands<String, String> setup = client.connect().sync();
            setup.set(stockKey, String.valueOf(STOCK));
            setup.del(buyersKey);
            setup.getStatefulConnection().close();

            CountDownLatch ready = new CountDownLatch(USERS);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(USERS);
            AtomicInteger success = new AtomicInteger();
            AtomicInteger soldOut = new AtomicInteger();
            AtomicInteger already = new AtomicInteger();
            for (int userId = 1; userId <= USERS; userId++)
            {
                StatefulRedisConnection<String, String> connection = client.connect();
                connections.add(connection);
                int buyer = userId;
                Thread thread = new Thread(() ->
                {
                    ready.countDown();
                    try
                    {
                        start.await();
                        Long result = connection.sync().eval(SeckillStock.BUY_LUA, ScriptOutputType.INTEGER,
                                new String[] { stockKey, buyersKey }, SeckillStock.buyerLabel(buyer));
                        if (result != null && result >= 0)
                        {
                            success.incrementAndGet();
                        }
                        else if (result != null && result == -1L)
                        {
                            soldOut.incrementAndGet();
                        }
                        else if (result != null && result == -2L)
                        {
                            already.incrementAndGet();
                        }
                    }
                    catch (InterruptedException ex)
                    {
                        Thread.currentThread().interrupt();
                    }
                    finally
                    {
                        done.countDown();
                    }
                });
                thread.start();
            }
            ready.await(30, TimeUnit.SECONDS);
            long began = System.nanoTime();
            start.countDown();
            done.await(30, TimeUnit.SECONDS);
            long elapsedMs = (System.nanoTime() - began) / 1_000_000L;
            RedisCommands<String, String> check = client.connect().sync();
            String left = check.get(stockKey);
            Long buyers = check.scard(buyersKey);
            check.set("seckill:load:summary", "success=" + success.get()
                    + " soldOut=" + soldOut.get()
                    + " already=" + already.get()
                    + " left=" + left
                    + " buyers=" + buyers
                    + " ms=" + elapsedMs);
            java.util.Set<String> names = check.smembers(buyersKey);
            check.getStatefulConnection().close();
            System.out.println("concurrent success=" + success.get() + " soldOut=" + soldOut.get()
                    + " already=" + already.get() + " left=" + left + " buyers=" + buyers + " ms=" + elapsedMs);
            assertEquals(STOCK, success.get());
            assertEquals(USERS - STOCK, soldOut.get());
            assertEquals(0, already.get());
            assertEquals("0", left);
            assertEquals((long) STOCK, buyers);
            for (String name : names)
            {
                assertTrue(name.startsWith("\u7528\u6237"));
            }
        }
        finally
        {
            for (StatefulRedisConnection<String, String> connection : connections)
            {
                connection.close();
            }
            client.shutdown();
        }
    }
}
