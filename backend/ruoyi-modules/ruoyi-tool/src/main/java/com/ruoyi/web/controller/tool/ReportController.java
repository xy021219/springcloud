package com.ruoyi.web.controller.tool;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.web.core.report.AiReport;

@RestController
@RequestMapping("/tool/report")
public class ReportController extends BaseController
{
    @RequiresPermissions("tool:report:list")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return AjaxResult.success(AiReport.sample());
    }
}
