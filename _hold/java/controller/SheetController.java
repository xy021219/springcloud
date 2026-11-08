package com.ruoyi.web.controller.tool;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.web.core.sheet.SheetExportRequest;
import com.ruoyi.web.core.sheet.SheetWorkbook;

/**
 * 上传表格，改两列，再导出。
 */
@RestController
@RequestMapping("/tool/sheet")
public class SheetController extends BaseController
{
    @PreAuthorize("@ss.hasPermi('tool:sheet:list')")
    @PostMapping("/preview")
    public AjaxResult preview(MultipartFile file)
    {
        return AjaxResult.success(SheetWorkbook.read(file));
    }

    @PreAuthorize("@ss.hasPermi('tool:sheet:list')")
    @Log(title = "改表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@RequestBody SheetExportRequest request, HttpServletResponse response) throws Exception
    {
        byte[] body = SheetWorkbook.write(request);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        FileUtils.setAttachmentResponseHeader(response, "edited.xlsx");
        response.getOutputStream().write(body);
    }
}
