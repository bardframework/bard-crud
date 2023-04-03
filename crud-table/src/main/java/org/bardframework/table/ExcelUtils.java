package org.bardframework.table;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.base.PagedData;
import org.bardframework.table.header.HeaderTemplate;
import org.bardframework.table.header.TableHeader;
import org.springframework.context.MessageSource;

import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class ExcelUtils {

    public static void generateExcel(TableTemplate tableTemplate, TableData tableData, OutputStream outputStream, Locale locale, boolean rtl, HttpServletRequest httpRequest) throws Exception {
        try (Workbook workbook = new SXSSFWorkbook()) {
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 10);
            headerFont.setFontName("Tahoma");
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_BLUE.getIndex());
            headerStyle.setAlignment(HorizontalAlignment.GENERAL);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Font dataFont = workbook.createFont();
            dataFont.setFontHeightInPoints((short) 10);
            dataFont.setFontName("Tahoma");
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setFont(dataFont);
            dataStyle.setAlignment(HorizontalAlignment.GENERAL);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            ExcelUtils.createSheet(workbook, tableTemplate, tableData, headerStyle, dataStyle, locale, httpRequest);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                workbook.getSheetAt(i).setRightToLeft(rtl);
            }
            workbook.write(outputStream);
        }
    }

    public static void createSheet(Workbook workbook, TableTemplate tableTemplate, TableData tableData, CellStyle headerStyle, CellStyle dataStyle, Locale locale, HttpServletRequest httpRequest) throws Exception {
        TableModel tableModel = TableUtils.toTable(tableTemplate, Map.of(), locale, httpRequest);
        Sheet sheet = workbook.createSheet(tableModel.getName());
        ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        int rowIndex = 0;
        Row headerRow = sheet.createRow(rowIndex++);
        /*
            write headers
        */
        int columnIndex = 0;
        for (TableHeader header : tableModel.getHeaders()) {
            Cell cell = headerRow.createCell(columnIndex++);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(header.getTitle());
        }

        /*
         ساخت استایل های مورد نیاز همه ی هدرها
         */
        Map<String, CellStyle> styles = new HashMap<>();
        for (HeaderTemplate<?, ?> headerModel : tableTemplate.getHeaderTemplates()) {
            if (null != headerModel.getExcelFormat()) {
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.cloneStyleFrom(dataStyle);
                cellStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat(headerModel.getExcelFormat()));
                styles.put(headerModel.getExcelFormat(), cellStyle);
            }
        }

        /*
            write data
        */
        for (Map<String, List<Object>> rowDataMap : tableData.getData()) {
            List<Object> rowData = rowDataMap.values().iterator().next();
            Row row = sheet.createRow(rowIndex++);
            for (int index = 0; index < tableTemplate.getHeaderTemplates().size(); index++) {
                Cell cell = row.createCell(index);
                Object cellValue = rowData.get(index);
                HeaderTemplate<?, ?> headerTemplate = tableTemplate.getHeaderTemplates().get(index);
                if (null == cellValue) {
                    cell.setBlank();
                } else {
                    if (Number.class.isAssignableFrom(cellValue.getClass())) {
                        cell.setCellValue(((Number) cellValue).doubleValue());
                    } else {
                        cell.setCellValue(cellValue.toString());
                    }
                }
                if (null != headerTemplate.getExcelFormat()) {
                    cell.setCellStyle(styles.get(headerTemplate.getExcelFormat()));
                } else {
                    cell.setCellStyle(dataStyle);
                }
            }
        }
        for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.createFreezePane(0, 1);
    }

    public static <M extends BaseModel<?>> TableData toTableData(PagedData<M> pagedData, TableTemplate tableTemplate, Locale locale, boolean export, Object user) {
        TableData tableData = new TableData();
        tableData.setTotal(pagedData.getTotal());
        tableData.setHeaders(tableTemplate.getHeaderTemplates().stream().map(TableHeader::getName).collect(Collectors.toList()));
        for (M model : pagedData.getData()) {
            List<Object> values = new ArrayList<>();
            for (HeaderTemplate headerTemplate : tableTemplate.getHeaderTemplates()) {
                Object value;
                try {
                    value = ReflectionUtils.getPropertyValue(model, headerTemplate.getName());
                } catch (Exception e) {
                    throw new IllegalStateException(String.format("can't read property [%s] of [%s] instance and convert it, table [%s]", headerTemplate.getName(), model.getClass(), tableTemplate.getName()), e);
                }
                try {
                    values.add(ExcelUtils.format(headerTemplate, value, locale, export, tableTemplate.getMessageSource()));
                } catch (Exception e) {
                    throw new IllegalStateException(String.format("error formatting value [%s] with formatter [%s], table [%s]", value, headerTemplate.getClass(), tableTemplate.getName()), e);
                }
            }
            tableData.addData(model.getId().toString(), values);
        }
        return tableData;
    }

    private Object format(HeaderTemplate headerTemplate, Object value, Locale locale, boolean export, MessageSource messageSource) {
        return export ? headerTemplate.formatForExport(value, locale, messageSource) : headerTemplate.format(value, locale, messageSource);
    }
}
