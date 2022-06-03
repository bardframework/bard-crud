package org.bardframework.crud.api.base;

import com.fasterxml.jackson.core.type.TypeReference;
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
public interface ReadRestControllerTest<M extends BaseModel<I>, C extends BaseCriteria<I>, P extends DataProviderService<M, C, ?, ?, ?, I, U>, I extends Comparable<? super I>, U> extends WebTest {

    P getDataProvider();

    TypeReference<M> getModelTypeReference();

    TypeReference<? extends PagedData<M>> getDataModelTypeReference();

    String BASE_URL();

    String GET_URL(I id);

    default String FILTER_URL() {
        return BASE_URL() + "/" + FILTER_URL;
    }

    default MockHttpServletRequestBuilder FILTER(C criteria, Pageable pageable) throws Exception {
        return MockMvcRequestBuilders.post(this.FILTER_URL()).content(this.getObjectMapper().writeValueAsString(criteria)).contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    default void testFilter() throws Exception {
        /*
          to be sure at least one model exist.
         */
        U user = this.getDataProvider().getUser();
        this.getDataProvider().getModel(user);
        MockHttpServletRequestBuilder request = this.FILTER(this.getDataProvider().getCriteria(), this.getDataProvider().getPageable());
        PagedData<M> response = this.executeOk(request, this.getDataModelTypeReference());
        assertThat(response.getTotal()).isGreaterThan(0);
    }

    @Test
    default void testGET() throws Exception {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(this.GET_URL(id));
        M result = this.executeOk(request, getModelTypeReference());
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    default void testGETInvalidId() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(this.GET_URL(this.getDataProvider().getInvalidId()));
        execute(request, getModelTypeReference(), HttpStatus.NOT_FOUND);
    }
}
