package org.bardframework.crud.api.base;

import com.fasterxml.jackson.databind.JavaType;
import org.bardframework.commons.web.WebTestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created on 14/05/2017.
 */
public interface WriteRestControllerTest<M extends BaseModel<I>, D, P extends ServiceDataProvider<M, ?, D, ?, ?, I, U>, I, U> extends WebTestHelper {

    String BASE_URL();

    P getDataProvider();

    Class<M> getModelClass();

    default String GET_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    default String DELETE_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    default String SAVE_URL() {
        return BASE_URL();
    }

    default String UPDATE_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    default JavaType getModelJavaType() {
        return this.getObjectMapper().getTypeFactory().constructType(this.getModelClass());
    }

    default JavaType getLongJavaType() {
        return this.getObjectMapper().getTypeFactory().constructType(Long.class);
    }

    @Test
    default void testSAVE() throws Exception {
        D dto = this.getDataProvider().getDto();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(SAVE_URL())
                .content(this.getObjectMapper().writeValueAsBytes(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        M result = execute(request, HttpStatus.OK, getModelJavaType());
        assertThat(result.getId()).isNotNull();
        this.getDataProvider().assertEqualSave(result, dto);
    }

    @Test
    default void testSAVEUnsuccessful() throws Exception {
        D invalidDto = this.getDataProvider().getInvalidDto();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(SAVE_URL())
                .content(this.getObjectMapper().writeValueAsBytes(invalidDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        MvcResult response = this.execute(request);
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
    }

    @Test
    default void testUPDATE() throws Exception {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        D dto = this.getDataProvider().getDto();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(this.UPDATE_URL(id))
                .content(this.getObjectMapper().writeValueAsBytes(dto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        M response = this.execute(request, HttpStatus.OK, getModelJavaType());
        this.getDataProvider().assertEqualUpdate(response, dto);
    }

    @Test
    default void testUPDATEUnsuccessful() throws Exception {
        U user = this.getDataProvider().getUser();
        I id = this.getDataProvider().getId(user);
        D invalidDto = this.getDataProvider().getInvalidDto();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(this.UPDATE_URL(id))
                .content(this.getObjectMapper().writeValueAsBytes(invalidDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        MvcResult response = this.execute(request);
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
    }

    @Test
    default void testDELETE() throws Exception {
        U user = this.getDataProvider().getUser();
        M savedModel = this.getDataProvider().saveNew(1, user).get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(this.DELETE_URL(savedModel.getId()));
        this.execute(request, HttpStatus.OK, this.getLongJavaType());
        MvcResult response = this.execute(MockMvcRequestBuilders.get(this.GET_URL(savedModel.getId())));
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    default void testDELETEUnsuccessful() throws Exception {
        I invalidId = this.getDataProvider().getInvalidId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(this.DELETE_URL(invalidId));
        this.execute(request, HttpStatus.NOT_FOUND, this.getLongJavaType());
    }
}
