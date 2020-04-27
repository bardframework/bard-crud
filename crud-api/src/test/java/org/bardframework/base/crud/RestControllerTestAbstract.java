package org.bardframework.base.crud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bardframework.base.crud.ReadRestController.FILTER_URL;

/**
 * Created by Sama-PC on 14/05/2017.
 */
public interface RestControllerTestAbstract<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, D, P extends DataProviderServiceAbstract<M, C, D, ?, ?, I, U>, I extends Comparable<? super I>, U> extends WebTest {

    default String GET_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    default String DELETE_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    default String FILTER_URL() {
        return BASE_URL() + "/" + FILTER_URL;
    }

    default String SAVE_URL() {
        return BASE_URL() + "/";
    }

    default String UPDATE_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    default MockHttpServletRequestBuilder SAVE(D dto) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(SAVE_URL())
                .content(this.getObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON);
    }

    default MockHttpServletRequestBuilder GET(I id)
            throws Exception {
        return MockMvcRequestBuilders.get(this.GET_URL(id));
    }

    default MockHttpServletRequestBuilder FILTER(C criteria, Pageable pageable) throws JsonProcessingException {
        return MockMvcRequestBuilders.post(this.FILTER_URL())
                .content(this.getObjectMapper().writeValueAsString(criteria))
                .contentType(MediaType.APPLICATION_JSON);
    }

    default MockHttpServletRequestBuilder UPDATE(I id, D dto) throws JsonProcessingException {
        return MockMvcRequestBuilders.put(this.UPDATE_URL(id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.getObjectMapper().writeValueAsString(dto));
    }

    default MockHttpServletRequestBuilder DELETE(I id) {
        return MockMvcRequestBuilders.delete(this.DELETE_URL(id));
    }

    default TypeReference<Long> getDeleteTypeReference() {
        return new TypeReference<Long>() {
        };
    }

    @Test
    default void testFilter()
            throws Exception {
        /*
          to be sure at least one model exist.
         */
        this.getDataProvider().getModel(this.getUser());
        MockHttpServletRequestBuilder request = this.FILTER(this.getDataProvider().getCriteria(), this.getDataProvider().getPageable());
        Page<M> response = executeOk(request, getDataModelTypeReference());
        assertThat(response.getTotalElements()).isGreaterThan(0);
    }

    @Test
    default void testGET()
            throws Exception {
        I id = this.getDataProvider().getId(this.getUser());
        MockHttpServletRequestBuilder request = this.GET(id);
        M result = executeOk(request, getModelTypeReference());
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    default void testGETInvalidId()
            throws Exception {
        MockHttpServletRequestBuilder request = this.GET(this.getDataProvider().getInvalidId());
        M result = executeOk(request, getModelTypeReference());
        assertThat(result).isNull();
    }

    @Test
    default void testSAVE()
            throws Exception {
        D dto = this.getDataProvider().getDto(this.getUser());
        MockHttpServletRequestBuilder request = this.SAVE(dto);
        M result = execute(request, getModelTypeReference(), HttpStatus.CREATED);
        assertThat(result.getId()).isNotNull();
        this.getDataProvider().assertEqualSave(result, dto);
    }

    @Test
    default void testSAVEUnsuccessful()
            throws Exception {
        MockHttpServletRequestBuilder request = this.SAVE(this.getDataProvider().getUnsavedInvalidDto(this.getUser()));
        MvcResult response = executeNotAcceptable(request);
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
    }

    @Test
    default void testUPDATE()
            throws Exception {
        I id = this.getDataProvider().getId(this.getUser());
        D dto = this.getDataProvider().getDto(this.getUser());
        MockHttpServletRequestBuilder request = this.UPDATE(id, dto);
        M response = executeOk(request, getModelTypeReference());
        this.getDataProvider().assertEqualUpdate(response, dto);
    }

    @Test
    default void testUPDATEUnsuccessful()
            throws Exception {
        I id = this.getDataProvider().getId(this.getUser());
        MockHttpServletRequestBuilder request = this.UPDATE(id, this.getDataProvider().getInvalidDto(this.getUser()));
        MvcResult response = executeNotAcceptable(request);
        assertThat(response.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
    }

    @Test
    default void testDELETE()
            throws Exception {
        M savedModel = this.getDataProvider().saveNew(1, this.getUser()).get(0);
        MockHttpServletRequestBuilder request = this.DELETE(savedModel.getId());
        executeOk(request, this.getDeleteTypeReference());
    }

    @Test
    default void testDELETEUnsuccessful()
            throws Exception {
        MockHttpServletRequestBuilder request = this.DELETE(this.getDataProvider().getInvalidId());
        execute(request, this.getDeleteTypeReference(), HttpStatus.NO_CONTENT);
    }

    U getUser();

    String BASE_URL();

    P getDataProvider();

    TypeReference<M> getModelTypeReference();

    TypeReference<? extends Page<M>> getDataModelTypeReference();
}
