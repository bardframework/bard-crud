package org.bardframework.table;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.base.BaseService;
import org.bardframework.crud.api.base.PagedData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

public interface TableModelRestController<M extends BaseModel<?>, C extends BaseCriteria<?>, S extends BaseService<M, C, ?, ?, ?, U>, U> {

    String APPLICATION_OOXML_SHEET = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    String TABLE_GET_URL = "table";
    String TABLE_FILTER_URL = "table/filter";
    String TABLE_EXPORT_URL = "table/export";

    TableTemplate getTableTemplate();

    S getService();

    U getUser();

    boolean isRtl(Locale locale, U user);

    String getExportFileName(String contentType, Locale locale, U user);

    @GetMapping(path = TABLE_GET_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    default TableModel getTableModel(Locale locale, HttpServletRequest httpRequest) throws Exception {
        return TableUtils.toTable(this.getTableTemplate(), Map.of(), locale, httpRequest);
    }

    @GetMapping(path = TABLE_FILTER_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    default TableData getTableData(@ModelAttribute C criteria, Pageable page, Locale locale) {
        U user = this.getUser();
        PagedData<M> pagedData = this.getService().get(criteria, page, user);
        return ExcelUtils.toTableData(pagedData, this.getTableTemplate(), locale, false, user);
    }

    @GetMapping(path = TABLE_EXPORT_URL, produces = APPLICATION_OOXML_SHEET)
    default void exportTable(@ModelAttribute C criteria, Locale locale, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws Exception {
        U user = this.getUser();
        PagedData<M> pagedData = this.getService().get(criteria, QPageRequest.of(1, Integer.MAX_VALUE), user);
        TableData tableData = ExcelUtils.toTableData(pagedData, this.getTableTemplate(), locale, true, user);
        String fileName = this.getExportFileName(APPLICATION_OOXML_SHEET, locale, user);
        try (OutputStream outputStream = httpResponse.getOutputStream()) {
            httpResponse.setContentType(APPLICATION_OOXML_SHEET);
            httpResponse.addHeader("Content-Disposition", "attachment;filename=\"%s\"".formatted(fileName));
            ExcelUtils.generateExcel(this.getTableTemplate(), tableData, outputStream, locale, this.isRtl(locale, user), httpRequest);
        }
    }
}
