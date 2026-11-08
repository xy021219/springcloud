package com.ruoyi.web.core.seckill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class SeckillLoadService
{
    static final int USERS = 200;

    static final String SUMMARY_KEY = "seckill:load:summary";

    static final String INSIGHT_URL = "http://127.0.0.1:5540/";

    private final StringRedisTemplate redis;

    private final SeckillService seckillService;

    private final DefaultRedisScript<Long> buyScript;

    public SeckillLoadService(StringRedisTemplate redis, SeckillService seckillService)
    {
        this.redis = redis;
        this.seckillService = seckillService;
        this.buyScript = new DefaultRedisScript<>();
        this.buyScript.setResultType(Long.class);
        this.buyScript.setScriptText(SeckillStock.BUY_LUA);
    }

    public SeckillLoadView current()
    {
        return read();
    }

    public synchronized SeckillLoadView adjust(SeckillStockForm form)
    {
        SeckillProduct product = seckillService.product();
        int currentRedis = currentRedis(product.getCode());
        int[] next = SeckillSale.nextStocks(currentRedis, product.getDbStock(), form.getRedisStock(), form.getDbStock(),
                increase(form.getRedisMode()), increase(form.getDbMode()));
        seckillService.saveStocks(next[0], next[1], false, false);
        clearSummary();
        return read();
    }

    public synchronized SeckillLoadView returnOrders()
    {
        seckillService.returnSoldStock();
        clearSummary();
        return read();
    }

    public synchronized SeckillLoadView zero()
    {
        seckillService.saveStocks(0, 0, false, false);
        clearSummary();
        return read();
    }

    public synchronized SeckillLoadPair run() throws InterruptedException
    {
        SeckillProduct product = seckillService.product();
        SeckillLoadView before = read();
        String stockKey = SeckillStock.stockKey(product.getCode());
        String buyersKey = SeckillStock.buyersKey(product.getCode());
        CountDownLatch ready = new CountDownLatch(USERS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(USERS);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        AtomicInteger already = new AtomicInteger();
        for (int userId = 1; userId <= USERS; userId++)
        {
            int buyer = userId;
            Thread thread = new Thread(() ->
            {
                ready.countDown();
                try
                {
                    start.await();
                    Long result = redis.execute(buyScript, List.of(stockKey, buyersKey), SeckillStock.buyerLabel(buyer));
                    if (result != null && result >= 0)
                    {
                        boolean saved = seckillService.persist(SeckillService.ACTIVITY_ID, buyer,
                                "load-" + buyer + "-" + UUID.randomUUID(), product.getCode(), SeckillSale.orderNo());
                        if (saved)
                        {
                            success.incrementAndGet();
                        }
                        else
                        {
                            seckillService.restore(product.getCode(), buyer);
                            already.incrementAndGet();
                        }
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
        String left = redis.opsForValue().get(stockKey);
        Long buyers = redis.opsForSet().size(buyersKey);
        redis.opsForValue().set(SUMMARY_KEY, SeckillSale.waveSummary(success.get(), soldOut.get(), already.get(),
                left, buyers == null ? 0L : buyers, elapsedMs));
        SeckillLoadPair pair = new SeckillLoadPair();
        pair.setBefore(before);
        pair.setAfter(read());
        return pair;
    }

    private SeckillLoadView read()
    {
        SeckillProduct product = seckillService.product();
        SeckillLoadView view = new SeckillLoadView();
        view.setProductCode(product.getCode());
        view.setProductName(product.getName());
        view.setDbStock(String.valueOf(product.getDbStock()));
        String redisStock = redis.opsForValue().get(SeckillStock.stockKey(product.getCode()));
        view.setRedisStock(redisStock == null ? "0" : redisStock);
        String summary = redis.opsForValue().get(SUMMARY_KEY);
        view.setSummary(SeckillSale.displaySummary(summary));
        view.setOrderCount(seckillService.orderCount());
        Set<String> names = redis.opsForSet().members(SeckillStock.buyersKey(product.getCode()));
        List<String> buyers = new ArrayList<>();
        if (names != null)
        {
            buyers.addAll(names);
            buyers.sort(Comparator.comparingInt(SeckillLoadService::buyerOrder));
        }
        view.setBuyers(buyers);
        view.setInsightUrl(INSIGHT_URL);
        return view;
    }

    private void clearSummary()
    {
        redis.delete(SUMMARY_KEY);
        redis.delete("seckill:load:stock");
        redis.delete("seckill:load:buyers");
    }

    private int currentRedis(String productCode)
    {
        String value = redis.opsForValue().get(SeckillStock.stockKey(productCode));
        if (value == null || value.isEmpty())
        {
            return 0;
        }
        return Integer.parseInt(value);
    }

    private static boolean increase(String mode)
    {
        return !"sub".equals(mode);
    }

    private static int buyerOrder(String label)
    {
        String prefix = "\u7528\u6237";
        if (label != null && label.startsWith(prefix))
        {
            return Integer.parseInt(label.substring(prefix.length()));
        }
        return Integer.MAX_VALUE;
    }
}
