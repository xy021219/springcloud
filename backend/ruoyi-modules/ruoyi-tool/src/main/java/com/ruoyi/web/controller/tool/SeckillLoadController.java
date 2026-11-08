package com.ruoyi.web.controller.tool;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.web.core.seckill.SeckillLoadService;
import com.ruoyi.web.core.seckill.SeckillSale;
import com.ruoyi.web.core.seckill.SeckillStockForm;

@RestController
@RequestMapping("/tool/seckill/load")
public class SeckillLoadController
{
    private final SeckillLoadService loadService;

    public SeckillLoadController(SeckillLoadService loadService)
    {
        this.loadService = loadService;
    }

    @RequiresPermissions("tool:seckill:list")
    @GetMapping
    public AjaxResult current()
    {
        return AjaxResult.success(loadService.current());
    }

    @RequiresPermissions("tool:seckill:list")
    @PostMapping("/stock")
    public AjaxResult stock(@RequestBody SeckillStockForm form)
    {
        return AjaxResult.success("\u5df2\u4fee\u6539\u5e93\u5b58", loadService.adjust(form));
    }

    @RequiresPermissions("tool:seckill:list")
    @PostMapping("/run")
    public AjaxResult run() throws InterruptedException
    {
        return AjaxResult.success(loadService.run());
    }

    @RequiresPermissions("tool:seckill:list")
    @PostMapping("/reset")
    public AjaxResult reset()
    {
        return AjaxResult.success(SeckillSale.RESET, loadService.returnOrders());
    }

    @RequiresPermissions("tool:seckill:list")
    @PostMapping("/zero")
    public AjaxResult zero()
    {
        return AjaxResult.success("\u5e93\u5b58\u5df2\u5f52\u96f6", loadService.zero());
    }
}
