package org.bardframework.table;

import org.assertj.core.api.Assertions;
import org.bardframework.commons.web.WebTestHelper;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.base.ServiceDataProvider;
import org.bardframework.crud.api.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bardframework.table.TableModelRestController.*;

/**
 * Created on 14/05/2017.
 */
public interface TableModelRestControllerTest<L extends TableModelRestController<?, ?, ?, ?>, M extends BaseModel<I>, C extends BaseCriteria<I>, P extends ServiceDataProvider<M, C, ?, ?, ?, I, U>, I, U> extends WebTestHelper {

    L getController();

    P getDataProvider();

    List<Locale> getLocales();

    String BASE_URL();

    default String TABLE_GET_URL() {
        return BASE_URL() + "/" + TABLE_GET_URL;
    }

    default String TABLE_FILTER_URL() {
        return BASE_URL() + "/" + TABLE_FILTER_URL;
    }

    default String TABLE_EXPORT_URL() {
        return BASE_URL() + "/" + TABLE_EXPORT_URL;
    }


    @Test
    default void testTableGet() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(this.TABLE_GET_URL());
        TableModel tableModel = this.execute(request, HttpStatus.OK, TableModel.class);
        assertThat(tableModel).isNotNull();
        assertThat(tableModel.getHeaders()).isNotEmpty();
    }

    @Test
    default void testTableDataGet() throws Exception {
        U user = this.getDataProvider().getUser();
        /*
          to be sure at least one model exist.
         */
        this.getDataProvider().getModel(user);
        C criteria = this.getDataProvider().getFilterCriteria();
        Pageable pageable = this.getDataProvider().getPageable();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(TestUtils.setPage(this.TABLE_FILTER_URL(), pageable)).content(this.getObjectMapper().writeValueAsBytes(criteria)).contentType(MediaType.APPLICATION_JSON);
        TableData tableData = this.execute(request, HttpStatus.OK, TableData.class);
        assertThat(tableData.getHeaders()).isNotNull().isNotEmpty();
        assertThat(tableData.getData()).isNotNull().isNotEmpty();
        assertThat(tableData.getTotal()).isGreaterThan(0);
    }

    @Test
    default void testTableDataExport() throws Exception {
        U user = this.getDataProvider().getUser();
        /*
          to be sure at least one model exist.
         */
        this.getDataProvider().getModel(user);
        C criteria = this.getDataProvider().getFilterCriteria();
        Pageable pageable = this.getDataProvider().getPageable();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(TestUtils.setPage(this.TABLE_EXPORT_URL(), pageable))
                .content(this.getObjectMapper().writeValueAsBytes(criteria))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(APPLICATION_OOXML_SHEET);
        MvcResult result = this.execute(request);
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        //FIXME read and check excel file from response body
    }

    @Test
    default void checkTableModelI18n() {
        for (Locale locale : this.getLocales()) {
            List<String> notExistKeys = TableModeCheckUtils.checkI18nExistence(this.getController().getTableTemplate(), Map.of(), locale);
            Assertions.assertThat(notExistKeys).withFailMessage("these message translation not found in lang <%s>:\n<%s>\n", String.join("\n", notExistKeys), locale).isEmpty();
        }
    }

    @Test
    default void checkTableDefinitionValidity() {
        for (Locale locale : this.getLocales()) {
            TableModeCheckUtils.checkDefinitionValidity(this.getController().getTableTemplate(), Map.of(), locale);
        }
    }
}
