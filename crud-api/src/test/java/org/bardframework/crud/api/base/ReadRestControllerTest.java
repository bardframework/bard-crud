package org.bardframework.crud.api.base;

import com.fasterxml.jackson.core.type.TypeReference;
import org.bardframework.commons.web.WebTestHelper;
import org.bardframework.crud.api.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bardframework.crud.api.base.ReadRestController.FILTER_URL;

/**
 * Created by Sama-PC on 14/05/2017.
 */
public interface ReadRestControllerTest<M extends BaseModel<I>, C extends BaseCriteria<I>, P extends DataProviderService<M, C, ?, ?, ?, I, U>, I extends Comparable<? super I>, U> extends WebTestHelper {

    P getDataProvider();

    TypeReference<M> getModelTypeReference();

    TypeReference<? extends PagedData<M>> getDataModelTypeReference();

    String BASE_URL();

    default String GET_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    default String FILTER_URL() {
        return BASE_URL() + "/" + FILTER_URL;
    }

    @Test
    default void testFilter() throws Exception {
        U user = this.getDataProvider().getUser();
        /*
          to be sure at least one model exist.
         */
        this.getDataProvider().getModel(user);
        C criteria = this.getDataProvider().getFilterCriteria();
        Pageable pageable = this.getDataProvider().getPageable();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(TestUtils.setPage(this.FILTER_URL(), pageable)).content(this.getObjectMapper().writeValueAsBytes(criteria)).contentType(MediaType.APPLICATION_JSON);
        PagedData<M> response = this.execute(request, HttpStatus.OK, this.getDataModelTypeReference());
        assertThat(response.getTotal()).isGreaterThan(0);
    }

    @Test
    default void testGET() throws Exception {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(this.GET_URL(id));
        M result = this.execute(request, HttpStatus.OK, getModelTypeReference());
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    default void testGETInvalidId() throws Exception {
        I invalidId = this.getDataProvider().getInvalidId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(this.GET_URL(invalidId));
        this.execute(request, HttpStatus.NOT_FOUND, getModelTypeReference());
    }
}
