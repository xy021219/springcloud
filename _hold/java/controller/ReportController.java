package com.ruoyi.web.controller.tool;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.core.report.AiReport;

/**
 * 根据模拟数据展示开发报表。
 */
@RestController
@RequestMapping("/tool/report")
public class ReportController extends BaseController
{
    @PreAuthorize("@ss.hasPermi('tool:report:list')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return AjaxResult.success(AiReport.sample());
    }
}
