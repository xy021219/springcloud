package com.ruoyi.web.controller.tool;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.file.FileUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.web.core.sheet.SheetExportRequest;
import com.ruoyi.web.core.sheet.SheetJobs;
import com.ruoyi.web.core.sheet.SheetWorkbook;

@RestController
@RequestMapping("/tool/sheet")
public class SheetController extends BaseController
{
    @RequiresPermissions("tool:sheet:list")
    @PostMapping("/preview")
    public AjaxResult preview(MultipartFile file)
    {
        try (SheetJobs.Job job = SheetJobs.enter())
        {
            return AjaxResult.success(SheetWorkbook.read(file));
        }
    }

    @RequiresPermissions("tool:sheet:list")
    @Log(title = "\u6539\u8868", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@RequestBody SheetExportRequest request, HttpServletResponse response) throws Exception
    {
        try (SheetJobs.Job job = SheetJobs.enter())
        {
            byte[] body = SheetWorkbook.write(request);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            FileUtils.setAttachmentResponseHeader(response, "edited.xlsx");
            response.getOutputStream().write(body);
        }
    }
}
