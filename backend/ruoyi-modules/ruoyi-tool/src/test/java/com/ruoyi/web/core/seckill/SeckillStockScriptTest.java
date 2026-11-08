package com.ruoyi.web.core.seckill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

class SeckillStockScriptTest
{
    @Test
    void scriptKeepsOneStockForOneBuyer()
    {
        RedisClient client = RedisClient.create("redis://localhost:6379");
        try (StatefulRedisConnection<String, String> connection = client.connect())
        {
            RedisCommands<String, String> commands = connection.sync();
            String stock = "seckill:it:stock";
            String buyers = "seckill:it:buyers";
            commands.set(stock, "1");
            commands.del(buyers);
            Long first = commands.eval(SeckillStock.BUY_LUA, ScriptOutputType.INTEGER, new String[] { stock, buyers }, "7");
            Long second = commands.eval(SeckillStock.BUY_LUA, ScriptOutputType.INTEGER, new String[] { stock, buyers }, "8");
            assertEquals(0L, first);
            assertEquals(-1L, second);

            commands.set(stock, "5");
            commands.del(buyers);
            Long once = commands.eval(SeckillStock.BUY_LUA, ScriptOutputType.INTEGER, new String[] { stock, buyers }, "3");
            Long again = commands.eval(SeckillStock.BUY_LUA, ScriptOutputType.INTEGER, new String[] { stock, buyers }, "3");
            assertEquals(4L, once);
            assertEquals(-2L, again);
            commands.del(stock, buyers);
        }
        finally
        {
            client.shutdown();
        }
    }
}
