package com.ruoyi.web.core.sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.core.exception.ServiceException;

class SheetWorkbookTest
{
    private static final String NAME = "\u59d3\u540d";

    private static final String DEPT = "\u90e8\u95e8";

    private static final String NOTE = "\u5907\u6ce8";

    private static final String MARK = "\uff08\u5df2\u6539\uff09";

    private static final String RANGE = "\u8bf7\u9009\u62e9 1 \u5230 3 \u5217";

    @Test
    void oneEditedColumnMarksOnlyThatHeader() throws Exception
    {
        SheetExportRequest request = request(List.of(NAME, DEPT, NOTE), List.of(2), List.of("\u7532", "\u7814\u53d1", "\u65b0\u5907\u6ce8"));
        try (XSSFWorkbook workbook = workbook(SheetWorkbook.write(request)))
        {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            assertEquals(NAME, cell(formatter, sheet, 0, 0));
            assertEquals(DEPT, cell(formatter, sheet, 0, 1));
            assertEquals(NOTE + MARK, cell(formatter, sheet, 0, 2));
            assertEquals("\u65b0\u5907\u6ce8", cell(formatter, sheet, 1, 2));
            assertEquals("\u7814\u53d1", cell(formatter, sheet, 1, 1));
            assertEquals("\u7532", cell(formatter, sheet, 1, 0));
            assertEquals(NAME, request.getHeaders().get(0));
            assertEquals(NOTE, request.getHeaders().get(2));
        }
    }

    @Test
    void threeEditedColumnsCanExport() throws Exception
    {
        SheetExportRequest request = request(List.of(NAME, DEPT, NOTE), List.of(0, 1, 2), List.of("\u5f20\u4e09", "\u5e73\u53f0", "\u65b0\u5907\u6ce8"));
        try (XSSFWorkbook workbook = workbook(SheetWorkbook.write(request)))
        {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            assertEquals(NAME + MARK, cell(formatter, sheet, 0, 0));
            assertEquals(DEPT + MARK, cell(formatter, sheet, 0, 1));
            assertEquals(NOTE + MARK, cell(formatter, sheet, 0, 2));
            assertEquals("\u5f20\u4e09", cell(formatter, sheet, 1, 0));
        }
    }

    @Test
    void zeroOrFourColumnsAreRejected()
    {
        SheetExportRequest none = request(List.of(NAME, DEPT, NOTE), List.of(), List.of("\u7532", "\u7814\u53d1", "\u539f\u6587"));
        ServiceException empty = assertThrows(ServiceException.class, () -> SheetWorkbook.write(none));
        assertEquals(RANGE, empty.getMessage());

        SheetExportRequest four = request(List.of(NAME, DEPT, NOTE, "\u5176\u4ed6"), List.of(0, 1, 2, 3), List.of("\u7532", "\u7814\u53d1", "\u539f\u6587", "\u4fdd\u7559"));
        ServiceException tooMany = assertThrows(ServiceException.class, () -> SheetWorkbook.write(four));
        assertEquals(RANGE, tooMany.getMessage());
    }

    private static SheetExportRequest request(List<String> headers, List<Integer> editedColumns, List<String> row)
    {
        SheetExportRequest request = new SheetExportRequest();
        request.setHeaders(headers);
        request.setEditedColumns(editedColumns);
        request.setRows(List.of(row));
        return request;
    }

    private static XSSFWorkbook workbook(byte[] bytes) throws Exception
    {
        return new XSSFWorkbook(new ByteArrayInputStream(bytes));
    }

    private static String cell(DataFormatter formatter, Sheet sheet, int row, int column)
    {
        return formatter.formatCellValue(sheet.getRow(row).getCell(column));
    }
}
