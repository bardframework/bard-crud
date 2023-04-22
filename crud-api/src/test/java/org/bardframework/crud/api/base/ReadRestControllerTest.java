package org.bardframework.crud.api.base;

import com.fasterxml.jackson.databind.JavaType;
import org.bardframework.commons.web.WebTestHelper;
import org.bardframework.crud.api.common.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bardframework.crud.api.base.ReadRestController.FILTER_URL;

/**
 * Created on 14/05/2017.
 */
public interface ReadRestControllerTest<M extends BaseModel<I>, C extends BaseCriteria<I>, P extends ServiceDataProvider<M, C, ?, ?, ?, I, U>, I, U> extends WebTestHelper {

    P getDataProvider();

    String BASE_URL();

    default String GET_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    default String FILTER_URL() {
        return BASE_URL() + "/" + FILTER_URL;
    }

    Class<M> getModelClass();

    default JavaType getModelJavaType() {
        return this.getObjectMapper().getTypeFactory().constructType(this.getModelClass());
    }

    default JavaType getPagedDataJavaType() {
        return this.getObjectMapper().getTypeFactory().constructParametricType(PagedData.class, this.getModelClass());
    }

    default String makeUrl(String url, C criteria, Pageable pageable) throws ReflectiveOperationException {
        return String.format("%s?page=%d&size=%d&%s", url, pageable.getPageNumber(), pageable.getPageSize(), TestUtils.toQueryParam(criteria));
    }

    @Test
    default void testFilter() throws Exception {
        U user = this.getDataProvider().getUser();
        /*
          to be sure at least one model exist.
         */
        M model = this.getDataProvider().getModel(user);
        C criteria = this.getDataProvider().getFilterCriteria(List.of(model));
        Pageable pageable = this.getDataProvider().getPageable();
        String url = this.makeUrl(this.FILTER_URL(), criteria, pageable);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(url).accept(MediaType.APPLICATION_JSON);
        PagedData<M> response = this.execute(request, HttpStatus.OK, this.getPagedDataJavaType());
        assertThat(response.getTotal()).isGreaterThan(0);
    }

    @Test
    default void testGET() throws Exception {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(this.GET_URL(id)).accept(MediaType.APPLICATION_JSON);
        M result = this.execute(request, HttpStatus.OK, this.getModelJavaType());
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    default void testGETInvalidId() throws Exception {
        I invalidId = this.getDataProvider().getInvalidId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(this.GET_URL(invalidId)).accept(MediaType.APPLICATION_JSON);
        this.execute(request, HttpStatus.NOT_FOUND, this.getModelJavaType());
    }
}
