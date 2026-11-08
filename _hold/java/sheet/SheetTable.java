package com.ruoyi.web.core.sheet;

import java.util.ArrayList;
import java.util.List;

/**
 * 预览和导出共用的表内容。只保留文字。
 */
public class SheetTable
{
    private List<String> headers = new ArrayList<>();

    private List<List<String>> rows = new ArrayList<>();

    public List<String> getHeaders()
    {
        return headers;
    }

    public void setHeaders(List<String> headers)
    {
        this.headers = headers;
    }

    public List<List<String>> getRows()
    {
        return rows;
    }

    public void setRows(List<List<String>> rows)
    {
        this.rows = rows;
    }
}
