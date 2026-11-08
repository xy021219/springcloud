package com.ruoyi.web.core.seckill;

import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;

@Service
public class SeckillService
{
    static final long ACTIVITY_ID = 1L;

    private final JdbcTemplate jdbcTemplate;

    private final SeckillStock stock;

    private final RabbitTemplate rabbitTemplate;

    public SeckillService(JdbcTemplate jdbcTemplate, SeckillStock stock, RabbitTemplate rabbitTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.stock = stock;
        this.rabbitTemplate = rabbitTemplate;
    }

    public SeckillActivity activity()
    {
        SeckillActivity activity = load();
        SeckillProduct product = loadProduct();
        activity.setProductCode(product.getCode());
        activity.setProductName(product.getName());
        List<Long> bought = jdbcTemplate.queryForList(
                "select user_id from seckill_order where activity_id = ?", Long.class, ACTIVITY_ID);
        stock.ensure(product.getCode(), product.getDbStock(), bought);
        activity.setStock(stock.current(product.getCode()));
        return activity;
    }

    public SeckillProduct product()
    {
        return loadProduct();
    }

    public List<SeckillProduct> products()
    {
        return jdbcTemplate.query(
                "select product_code, product_name, db_stock from seckill_product order by product_code",
                (rs, row) -> {
                    SeckillProduct product = new SeckillProduct();
                    product.setCode(rs.getString("product_code"));
                    product.setName(rs.getString("product_name"));
                    product.setDbStock(rs.getInt("db_stock"));
                    product.setRedisStock(stock.current(product.getCode()));
                    return product;
                });
    }

    public List<SeckillOrderRow> orders(String productCode)
    {
        return jdbcTemplate.query(
                "select o.order_no, o.user_id, date_format(o.create_time, '%Y-%m-%d %H:%i:%s') as create_time, "
                        + "p.product_code, p.product_name from seckill_order o "
                        + "join seckill_product p on p.product_code = o.product_code "
                        + "where o.activity_id = ? and o.product_code = ? order by o.order_id desc",
                (rs, row) -> {
                    SeckillOrderRow order = new SeckillOrderRow();
                    order.setOrderNo(rs.getString("order_no"));
                    order.setBuyer(SeckillStock.buyerLabel(rs.getLong("user_id")));
                    order.setCreateTime(rs.getString("create_time"));
                    order.setProductCode(rs.getString("product_code"));
                    order.setProductName(rs.getString("product_name"));
                    return order;
                },
                ACTIVITY_ID, productCode);
    }

    @Idempotent
    public SeckillActivity buy(long userId, String requestId, String productCode)
    {
        SeckillProduct product = resolve(productCode);
        List<Long> bought = jdbcTemplate.queryForList(
                "select user_id from seckill_order where activity_id = ? and product_code = ?",
                Long.class, ACTIVITY_ID, product.getCode());
        stock.markBought(product.getCode(), bought);
        int left = stock.tryBuy(product.getCode(), userId);
        SeckillActivity activity = load();
        activity.setProductCode(product.getCode());
        activity.setProductName(product.getName());
        activity.setStock(left);
        String orderNo = SeckillSale.orderNo();
        try
        {
            rabbitTemplate.convertAndSend(SeckillAmqpConfig.QUEUE, ACTIVITY_ID + "," + userId + "," + requestId + ","
                    + product.getCode() + "," + orderNo);
        }
        catch (RuntimeException ex)
        {
            stock.restore(product.getCode(), userId);
            throw new ServiceException(SeckillSale.RETRY);
        }
        return activity;
    }

    private SeckillProduct resolve(String productCode)
    {
        if (productCode == null || productCode.isBlank())
        {
            return loadProduct();
        }
        return jdbcTemplate.queryForObject(
                "select product_code, product_name, db_stock from seckill_product where product_code = ?",
                (rs, row) -> {
                    SeckillProduct product = new SeckillProduct();
                    product.setCode(rs.getString("product_code"));
                    product.setName(rs.getString("product_name"));
                    product.setDbStock(rs.getInt("db_stock"));
                    return product;
                },
                productCode);
    }

    @Transactional
    public boolean persist(long activityId, long userId, String requestId, String productCode, String orderNo)
    {
        if (productCode == null || productCode.isEmpty())
        {
            productCode = loadProduct().getCode();
        }
        if (orderNo == null || orderNo.isEmpty())
        {
            orderNo = SeckillSale.orderNo();
        }
        try
        {
            jdbcTemplate.update(
                    "insert into seckill_order (activity_id, user_id, request_id, create_time, order_no, product_code) "
                            + "values (?, ?, ?, now(), ?, ?)",
                    activityId, userId, requestId, orderNo, productCode);
        }
        catch (DuplicateKeyException ex)
        {
            return false;
        }
        jdbcTemplate.update("update seckill_activity set stock = stock - 1 where activity_id = ?", activityId);
        jdbcTemplate.update("update seckill_product set db_stock = db_stock - 1 where product_code = ?", productCode);
        return true;
    }

    public void restore(String productCode, long userId)
    {
        stock.restore(productCode, userId);
    }

    public int orderCount()
    {
        SeckillProduct product = loadProduct();
        Integer sold = jdbcTemplate.queryForObject(
                "select count(*) from seckill_order where activity_id = ? and product_code = ?",
                Integer.class, ACTIVITY_ID, product.getCode());
        return sold == null ? 0 : sold;
    }

    @Transactional
    public void saveStocks(int redisStock, int dbStock, boolean deleteOrders, boolean clearBuyers)
    {
        if (redisStock < 0 || dbStock < 0)
        {
            throw new ServiceException(SeckillSale.STOCK_NEGATIVE);
        }
        SeckillSale.limit(redisStock, dbStock);
        SeckillProduct product = loadProduct();
        jdbcTemplate.update("update seckill_product set db_stock = ? where product_code = ?", dbStock, product.getCode());
        jdbcTemplate.update("update seckill_activity set stock = ? where activity_id = ?", dbStock, ACTIVITY_ID);
        if (deleteOrders)
        {
            jdbcTemplate.update("delete from seckill_order where activity_id = ? and product_code = ?",
                    ACTIVITY_ID, product.getCode());
        }
        stock.put(product.getCode(), redisStock);
        if (clearBuyers)
        {
            stock.clearBuyers(product.getCode());
        }
    }

    @Transactional
    public void returnSoldStock()
    {
        SeckillProduct product = loadProduct();
        int sold = orderCount();
        jdbcTemplate.update("delete from seckill_order where activity_id = ? and product_code = ?",
                ACTIVITY_ID, product.getCode());
        jdbcTemplate.update("update seckill_product set db_stock = db_stock + ? where product_code = ?",
                sold, product.getCode());
        jdbcTemplate.update("update seckill_activity set stock = stock + ? where activity_id = ?",
                sold, ACTIVITY_ID);
        stock.clearBuyers(product.getCode());
    }

    @Transactional
    public SeckillActivity cancel(long userId, String productCode)
    {
        SeckillProduct product = resolve(productCode);
        int deleted = jdbcTemplate.update(
                "delete from seckill_order where activity_id = ? and user_id = ? and product_code = ?",
                ACTIVITY_ID, userId, product.getCode());
        boolean hadMark = stock.unmark(product.getCode(), userId);
        if (!SeckillSale.shouldReturnOne(deleted > 0, hadMark))
        {
            throw new ServiceException(SeckillSale.NOTHING);
        }
        int left = stock.addOne(product.getCode());
        jdbcTemplate.update("update seckill_product set db_stock = db_stock + 1 where product_code = ?", product.getCode());
        jdbcTemplate.update("update seckill_activity set stock = stock + 1 where activity_id = ?", ACTIVITY_ID);
        SeckillActivity activity = load();
        activity.setProductCode(product.getCode());
        activity.setProductName(product.getName());
        activity.setStock(left);
        return activity;
    }

    @Transactional
    public SeckillActivity reset()
    {
        SeckillProduct product = loadProduct();
        jdbcTemplate.update("delete from seckill_order where activity_id = ?", ACTIVITY_ID);
        jdbcTemplate.update("update seckill_activity set stock = ? where activity_id = ?",
                SeckillSale.stockAfterReset(), ACTIVITY_ID);
        jdbcTemplate.update("update seckill_product set db_stock = ? where product_code = ?",
                SeckillSale.stockAfterReset(), product.getCode());
        stock.reset(product.getCode(), SeckillSale.stockAfterReset());
        return activity();
    }

    private SeckillActivity load()
    {
        return jdbcTemplate.queryForObject(
                "select activity_id, activity_name, stock from seckill_activity where activity_id = ?",
                (rs, row) -> {
                    SeckillActivity activity = new SeckillActivity();
                    activity.setActivityId(rs.getLong("activity_id"));
                    activity.setName(rs.getString("activity_name"));
                    activity.setStock(rs.getInt("stock"));
                    return activity;
                },
                ACTIVITY_ID);
    }

    private SeckillProduct loadProduct()
    {
        return jdbcTemplate.queryForObject(
                "select p.product_code, p.product_name, p.db_stock from seckill_activity a "
                        + "join seckill_product p on p.product_code = a.product_code where a.activity_id = ?",
                (rs, row) -> {
                    SeckillProduct product = new SeckillProduct();
                    product.setCode(rs.getString("product_code"));
                    product.setName(rs.getString("product_name"));
                    product.setDbStock(rs.getInt("db_stock"));
                    return product;
                },
                ACTIVITY_ID);
    }
}
