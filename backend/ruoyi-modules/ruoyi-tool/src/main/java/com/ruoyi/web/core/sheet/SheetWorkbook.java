package com.ruoyi.web.core.sheet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;

/**
 * \u53ea\u8bfb\u5199\u7b2c\u4e00\u4e2a\u5de5\u4f5c\u8868\u7684\u663e\u793a\u6587\u5b57\u3002\u4e0d\u4fdd\u7559\u6837\u5f0f\u3001\u516c\u5f0f\u548c\u5176\u4f59\u5de5\u4f5c\u8868\u3002
 */
public final class SheetWorkbook
{
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final int MAX_ROWS = 500;

    private static final int MAX_COLUMNS = 50;

    private static final String SELECT_COLUMNS = "\u8bf7\u9009\u62e9 1 \u5230 3 \u5217";

    private static final String EDITED_MARK = "\uff08\u5df2\u6539\uff09";

    private SheetWorkbook()
    {
    }

    public static SheetTable read(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("\u8bf7\u9009\u62e9\u6587\u4ef6");
        }
        if (file.getSize() > MAX_BYTES)
        {
            throw new ServiceException("\u6587\u4ef6\u8d85\u8fc7 5 MB");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase(Locale.ROOT).endsWith(".xlsx"))
        {
            throw new ServiceException("\u53ea\u652f\u6301 .xlsx \u6587\u4ef6");
        }
        try (InputStream input = file.getInputStream(); Workbook workbook = new XSSFWorkbook(input))
        {
            if (workbook.getNumberOfSheets() < 1)
            {
                throw new ServiceException("\u6ca1\u6709\u8868\u5934");
            }
            return readSheet(workbook.getSheetAt(0));
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("\u65e0\u6cd5\u8bfb\u53d6\u8fd9\u4e2a\u6587\u4ef6\uff0c\u8bf7\u4e0a\u4f20 .xlsx");
        }
    }

    public static byte[] write(SheetExportRequest request)
    {
        SheetTable table = validateExport(request);
        List<String> headers = markedHeaders(table.getHeaders(), request.getEditedColumns());
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            Sheet sheet = workbook.createSheet("Sheet1");
            Row headerRow = sheet.createRow(0);
            for (int column = 0; column < headers.size(); column++)
            {
                headerRow.createCell(column).setCellValue(headers.get(column));
            }
            for (int rowIndex = 0; rowIndex < table.getRows().size(); rowIndex++)
            {
                Row row = sheet.createRow(rowIndex + 1);
                List<String> values = table.getRows().get(rowIndex);
                for (int column = 0; column < values.size(); column++)
                {
                    row.createCell(column).setCellValue(values.get(column));
                }
            }
            workbook.write(output);
            return output.toByteArray();
        }
        catch (IOException e)
        {
            throw new ServiceException("\u5bfc\u51fa\u5931\u8d25");
        }
    }

    private static SheetTable readSheet(Sheet sheet)
    {
        if (sheet == null)
        {
            throw new ServiceException("\u6ca1\u6709\u8868\u5934");
        }
        Row headerRow = sheet.getRow(0);
        if (headerRow == null)
        {
            throw new ServiceException("\u6ca1\u6709\u8868\u5934");
        }
        DataFormatter formatter = new DataFormatter();
        List<Integer> columnIndexes = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        short lastCell = headerRow.getLastCellNum();
        for (int column = 0; column < lastCell; column++)
        {
            String header = text(formatter, headerRow.getCell(column));
            if (StringUtils.isBlank(header))
            {
                continue;
            }
            columnIndexes.add(column);
            headers.add(header);
        }
        if (headers.isEmpty())
        {
            throw new ServiceException("\u6ca1\u6709\u8868\u5934");
        }
        if (headers.size() > MAX_COLUMNS)
        {
            throw new ServiceException("\u5217\u6570\u4e0d\u80fd\u8d85\u8fc7 50");
        }
        int lastRow = sheet.getLastRowNum();
        if (lastRow < 1)
        {
            throw new ServiceException("\u6ca1\u6709\u6570\u636e\u884c");
        }
        if (lastRow > MAX_ROWS)
        {
            throw new ServiceException("\u6570\u636e\u4e0d\u80fd\u8d85\u8fc7 500 \u884c");
        }
        List<List<String>> rows = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= lastRow; rowIndex++)
        {
            Row row = sheet.getRow(rowIndex);
            List<String> values = new ArrayList<>();
            for (Integer column : columnIndexes)
            {
                values.add(row == null ? "" : text(formatter, row.getCell(column)));
            }
            rows.add(values);
        }
        SheetTable table = new SheetTable();
        table.setHeaders(headers);
        table.setRows(rows);
        return table;
    }

    private static SheetTable validateExport(SheetExportRequest request)
    {
        if (request == null || request.getHeaders() == null || request.getHeaders().isEmpty())
        {
            throw new ServiceException("\u6ca1\u6709\u8868\u5934");
        }
        if (request.getHeaders().size() > MAX_COLUMNS)
        {
            throw new ServiceException("\u5217\u6570\u4e0d\u80fd\u8d85\u8fc7 50");
        }
        if (request.getRows() == null || request.getRows().isEmpty())
        {
            throw new ServiceException("\u6ca1\u6709\u6570\u636e\u884c");
        }
        if (request.getRows().size() > MAX_ROWS)
        {
            throw new ServiceException("\u6570\u636e\u4e0d\u80fd\u8d85\u8fc7 500 \u884c");
        }
        List<Integer> editedColumns = request.getEditedColumns();
        Set<Integer> unique = new HashSet<>();
        if (editedColumns != null)
        {
            for (Integer column : editedColumns)
            {
                if (column == null || column < 0 || column >= request.getHeaders().size())
                {
                    throw new ServiceException(SELECT_COLUMNS);
                }
                unique.add(column);
            }
        }
        if (unique.size() < 1 || unique.size() > 3)
        {
            throw new ServiceException(SELECT_COLUMNS);
        }
        for (List<String> row : request.getRows())
        {
            if (row == null || row.size() != request.getHeaders().size())
            {
                throw new ServiceException("\u5217\u6570\u4e0e\u8868\u5934\u4e0d\u4e00\u81f4");
            }
        }
        SheetTable table = new SheetTable();
        table.setHeaders(request.getHeaders());
        table.setRows(request.getRows());
        return table;
    }

    private static List<String> markedHeaders(List<String> headers, List<Integer> editedColumns)
    {
        Set<Integer> edited = new HashSet<>();
        if (editedColumns != null)
        {
            edited.addAll(editedColumns);
        }
        List<String> marked = new ArrayList<>();
        for (int column = 0; column < headers.size(); column++)
        {
            String header = headers.get(column);
            if (edited.contains(column))
            {
                header = header + EDITED_MARK;
            }
            marked.add(header);
        }
        return marked;
    }

    private static String text(DataFormatter formatter, Cell cell)
    {
        if (cell == null)
        {
            return "";
        }
        String value = formatter.formatCellValue(cell);
        return value == null ? "" : value;
    }
}
