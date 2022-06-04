package org.bardframework.crud.api.base;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMapping;

public abstract class BaseCrudControllerTest<CL, P extends DataProviderService<?, ?, ?, ?, ?, I, ?>, I extends Comparable<? super I>> extends BaseRestControllerTest {

    @Autowired
    private P dataProvider;
    @Autowired
    private CL controller;

    public P getDataProvider() {
        return dataProvider;
    }

    public CL getController() {
        return controller;
    }

    public String BASE_URL() {
        return "/" + AopProxyUtils.ultimateTargetClass(this.getController()).getAnnotation(RequestMapping.class).value()[0];
    }

    public String GET_URL(I id) {
        return BASE_URL() + "/" + id;
    }

    public MockHttpServletRequestBuilder setAuthentication(MockHttpServletRequestBuilder requestBuilder) {
        return requestBuilder;
    }
}
