package com.ruoyi.web.core.sheet;

import java.util.ArrayList;
import java.util.List;

/**
 * 导出请求。未勾选的列由页面带回预览时的原文。
 */
public class SheetExportRequest
{
    private List<String> headers = new ArrayList<>();

    private List<List<String>> rows = new ArrayList<>();

    private List<Integer> editedColumns = new ArrayList<>();

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

    public List<Integer> getEditedColumns()
    {
        return editedColumns;
    }

    public void setEditedColumns(List<Integer> editedColumns)
    {
        this.editedColumns = editedColumns;
    }
}
