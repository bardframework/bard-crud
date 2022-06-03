package org.bardframework.crud.api.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Created by Vahid Zafari on 4/23/2017.
 */
public interface WebTest {

    Logger LOGGER = LoggerFactory.getLogger(WebTest.class);

    TestRestTemplate getRestTemplate();

    MockMvc getMockMvc();

    ObjectMapper getObjectMapper();

    MockHttpServletRequestBuilder setAuthentication(MockHttpServletRequestBuilder request);

    default <T> T executeOk(MockHttpServletRequestBuilder request, TypeReference<T> returnType)
            throws Exception {
        MvcResult result = this.execute(request, HttpStatus.OK);
        if (StringUtils.isNotBlank(result.getResponse().getContentAsString())) {
            return this.getObjectMapper().readValue(result.getResponse().getContentAsString(), returnType);
        } else {
            return null;
        }
    }

    default <T> T execute(MockHttpServletRequestBuilder request, TypeReference<T> returnType, HttpStatus expectedStatus)
            throws Exception {
        MvcResult result = this.execute(request, expectedStatus);
        if (StringUtils.isNotBlank(result.getResponse().getContentAsString())) {
            return this.getObjectMapper().readValue(result.getResponse().getContentAsString(), returnType);
        } else {
            return null;
        }
    }

    default MvcResult executeNotAcceptable(MockHttpServletRequestBuilder request)
            throws Exception {
        return this.execute(request, HttpStatus.NOT_ACCEPTABLE);
    }

    default MvcResult execute(MockHttpServletRequestBuilder request, HttpStatus expectedStatus)
            throws Exception {
        request = this.setAuthentication(request);
        request.accept(MediaType.APPLICATION_JSON);
        return this.executeWithoutAuthentication(request, expectedStatus);
    }

    default MvcResult executeWithoutAuthentication(MockHttpServletRequestBuilder request, HttpStatus expectedStatus)
            throws Exception {
        MvcResult result = this.getMockMvc().perform(request).andExpect(MockMvcResultMatchers.status().is(expectedStatus.value())).andReturn();
        LOGGER.info("calling '{}', status: {},result:\n{}", result.getRequest().getRequestURI(), result.getResponse().getStatus(), result.getResponse().getContentAsString());
        return result;
    }

    default <T> ResponseEntity<T> post(String uri, Object dto, Class<T> responseType, HttpStatus status) {
        ResponseEntity<T> responseEntity;
        responseEntity = this.getRestTemplate().exchange(uri, HttpMethod.POST, new HttpEntity<>(dto), responseType);
        LOGGER.info("response of calling [{}] is [{}]", uri, responseEntity.getBody());
        Assertions.assertThat(responseEntity.getStatusCodeValue()).isEqualTo(status.value());
        return responseEntity;
    }
}
