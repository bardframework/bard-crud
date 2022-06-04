package org.bardframework.crud.api.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public abstract class BaseRestControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestRestTemplate restTemplate;

    public MockMvc getMockMvc() {
        return mockMvc;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public TestRestTemplate getRestTemplate() {
        return restTemplate;
    }
}
