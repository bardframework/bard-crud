package org.bardframework.base.crud;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Created by Vahid Zafari on 4/23/2017.
 */
public interface WebTest {

    Logger LOGGER = LoggerFactory.getLogger(WebTest.class);

    default <T> T executeOk(MockHttpServletRequestBuilder request, TypeReference<T> returnType)
            throws Exception {
        return this.execute(request, returnType, HttpStatus.OK);
    }

    default <T> T executeNotAcceptable(MockHttpServletRequestBuilder request, TypeReference<T> returnType)
            throws Exception {
        return this.execute(request, returnType, HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * set <code>request.accept(MediaType.APPLICATION_JSON)</code>
     */
    default <T> T execute(MockHttpServletRequestBuilder request, TypeReference<T> returnType, HttpStatus expectedStatus)
            throws Exception {
        MvcResult result = this.execute(request, expectedStatus);
        return this.getObjectMapper().readValue(result.getResponse().getContentAsString(), returnType);
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

    MockMvc getMockMvc();

    ObjectMapper getObjectMapper();

    MockHttpServletRequestBuilder setAuthentication(MockHttpServletRequestBuilder request);
}