package org.bardframework.table;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.base.BaseService;
import org.bardframework.crud.api.base.PagedData;
import org.bardframework.table.header.HeaderTemplate;
import org.bardframework.table.header.TableHeader;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public interface TableModelRestController<M extends BaseModel<?>, C extends BaseCriteria<?>, S extends BaseService<M, C, ?, ?, ?, U>, U> {

    String APPLICATION_OOXML_SHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    String TABLE_GET_URL = "table";
    String TABLE_FILTER_URL = "table/filter";
    String TABLE_EXPORT_URL = "table/export";

    TableTemplate getTableTemplate();

    S getService();

    U getUser();

    boolean isRtl(Locale locale);

    String getExportFileName(String contentType, Locale locale, U user);

    @GetMapping(path = TABLE_GET_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    default TableModel getTableModel(Locale locale, HttpServletRequest httpRequest) throws Exception {
        return TableUtils.toTable(this.getTableTemplate(), Map.of(), locale, httpRequest);
    }

    @PostMapping(path = TABLE_FILTER_URL, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    default TableData getTableData(@RequestBody @Validated C criteria, Pageable page, Locale locale) {
        PagedData<M> pagedData = this.getService().get(criteria, page, this.getUser());
        return this.toTableData(pagedData, this.getTableTemplate(), locale, false, this.getUser());
    }

    @PostMapping(path = TABLE_EXPORT_URL, consumes = MediaType.APPLICATION_JSON_VALUE, produces = APPLICATION_OOXML_SHEET)
    default void exportTable(@RequestBody @Validated C criteria, Locale locale, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        PagedData<M> pagedData = this.getService().get(criteria, Pageable.ofSize(Integer.MAX_VALUE), this.getUser());
        TableData tableData = this.toTableData(pagedData, this.getTableTemplate(), locale, true, this.getUser());
        try (OutputStream outputStream = httpResponse.getOutputStream()) {
            httpResponse.setContentType(APPLICATION_OOXML_SHEET);
            httpResponse.addHeader("Content-Disposition", "attachment;filename=\"" + this.getExportFileName(APPLICATION_OOXML_SHEET, locale, this.getUser()) + " \"");
            ExcelUtils.generateExcel(this.getTableTemplate(), tableData, outputStream, locale, this.isRtl(locale), httpRequest);
        }
    }

    default TableData toTableData(PagedData<M> pagedData, TableTemplate tableTemplate, Locale locale, boolean export, U user) {
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
                    values.add(export ? headerTemplate.formatForExport(value, locale, tableTemplate.getMessageSource()) : headerTemplate.format(value, locale, tableTemplate.getMessageSource()));
                } catch (Exception e) {
                    throw new IllegalStateException(String.format("error formatting value [%s] with formatter [%s], table [%s]", value, headerTemplate.getClass(), tableTemplate.getName()), e);
                }
            }
            tableData.addData(model.getId().toString(), values);
        }
        return tableData;
    }
}
