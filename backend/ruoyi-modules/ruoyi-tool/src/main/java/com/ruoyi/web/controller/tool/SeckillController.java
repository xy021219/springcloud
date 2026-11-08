package com.ruoyi.web.controller.tool;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.web.core.seckill.SeckillSale;
import com.ruoyi.web.core.seckill.SeckillService;

@RestController
@RequestMapping("/tool/seckill")
public class SeckillController
{
    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService)
    {
        this.seckillService = seckillService;
    }

    @RequiresPermissions("tool:seckill:list")
    @GetMapping("/activity")
    public AjaxResult activity()
    {
        return AjaxResult.success(seckillService.activity());
    }

    @RequiresPermissions("tool:seckill:list")
    @GetMapping("/product")
    public AjaxResult product()
    {
        return AjaxResult.success(seckillService.products());
    }

    @RequiresPermissions("tool:seckill:list")
    @GetMapping("/orders")
    public AjaxResult orders(@RequestParam("productCode") String productCode)
    {
        return AjaxResult.success(seckillService.orders(productCode));
    }

    @RequiresPermissions("tool:seckill:list")
    @PostMapping("/order")
    public AjaxResult order(@RequestHeader("X-Request-Id") String requestId,
            @RequestHeader(value = "X-Product-Code", required = false) String productCode)
    {
        return AjaxResult.success(SeckillSale.SUCCESS, seckillService.buy(SecurityUtils.getUserId(), requestId, productCode));
    }

    @RequiresPermissions("tool:seckill:list")
    @PostMapping("/cancel")
    public AjaxResult cancel(@RequestParam("productCode") String productCode)
    {
        return AjaxResult.success(SeckillSale.CANCELLED, seckillService.cancel(SecurityUtils.getUserId(), productCode));
    }
}
