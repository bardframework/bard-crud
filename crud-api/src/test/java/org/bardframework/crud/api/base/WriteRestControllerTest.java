package org.bardframework.crud.api.base;

import com.fasterxml.jackson.core.type.TypeReference;
import org.bardframework.commons.web.WebTestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Sama-PC on 14/05/2017.
 */
public interface WriteRestControllerTest<M extends BaseModel<I>, D, P extends DataProviderService<M, ?, D, ?, ?, I, U>, I extends Comparable<? super I>, U> extends WebTestHelper {

    String BASE_URL();

    String GET_URL(I id);

    P getDataProvider();

    TypeReference<M> getModelTypeReference();

    default String DELETE_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    default String SAVE_URL() {
        return BASE_URL() + "/";
    }

    default String UPDATE_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    default TypeReference<Long> getDeleteTypeReference() {
        return new TypeReference<Long>() {
        };
    }

    default MockHttpServletRequestBuilder SAVE(D dto) throws Exception {
        return MockMvcRequestBuilders.post(SAVE_URL())
                .content(this.getObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON);
    }

    default MockHttpServletRequestBuilder UPDATE(I id, D dto) throws Exception {
        return MockMvcRequestBuilders.put(this.UPDATE_URL(id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.getObjectMapper().writeValueAsString(dto));
    }

    default MockHttpServletRequestBuilder DELETE(I id) throws Exception {
        return MockMvcRequestBuilders.delete(this.DELETE_URL(id));
    }

    @Test
    default void testSAVE() throws Exception {
        D dto = this.getDataProvider().getDto();
        MockHttpServletRequestBuilder request = this.SAVE(dto);
        M result = execute(request, HttpStatus.OK, getModelTypeReference());
        assertThat(result.getId()).isNotNull();
        this.getDataProvider().assertEqualSave(result, dto);
    }

    @Test
    default void testSAVEUnsuccessful() throws Exception {
        MockHttpServletRequestBuilder request = this.SAVE(this.getDataProvider().getInvalidDto());
        MvcResult response = this.execute(request);
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
    }

    @Test
    default void testUPDATE() throws Exception {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        D dto = this.getDataProvider().getDto();
        MockHttpServletRequestBuilder request = this.UPDATE(id, dto);
        M response = this.execute(request, HttpStatus.OK, getModelTypeReference());
        this.getDataProvider().assertEqualUpdate(response, dto);
    }

    @Test
    default void testUPDATEUnsuccessful() throws Exception {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        D invalidDto = this.getDataProvider().getInvalidDto();
        MockHttpServletRequestBuilder request = this.UPDATE(id, invalidDto);
        MvcResult response = this.execute(request);
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
    }

    @Test
    default void testDELETE() throws Exception {
        U user = this.getDataProvider().getUser();
        M savedModel = this.getDataProvider().saveNew(1, user).get(0);
        MockHttpServletRequestBuilder request = this.DELETE(savedModel.getId());
        this.execute(request, HttpStatus.OK, this.getDeleteTypeReference());
        MvcResult response = this.execute(MockMvcRequestBuilders.get(this.GET_URL(savedModel.getId())));
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    default void testDELETEUnsuccessful() throws Exception {
        MockHttpServletRequestBuilder request = this.DELETE(this.getDataProvider().getInvalidId());
        execute(request, HttpStatus.NOT_FOUND, this.getDeleteTypeReference());
    }
}
