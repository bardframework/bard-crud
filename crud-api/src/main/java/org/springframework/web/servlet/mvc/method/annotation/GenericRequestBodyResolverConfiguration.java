package org.springframework.web.servlet.mvc.method.annotation;

import com.fasterxml.jackson.databind.type.SimpleType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class GenericRequestBodyResolverConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericRequestBodyResolverConfiguration.class);

    private final RequestMappingHandlerAdapter mappingHandler;

    public GenericRequestBodyResolverConfiguration(RequestMappingHandlerAdapter mappingHandler) {
        this.mappingHandler = mappingHandler;
    }

    @PostConstruct
    public void addCustomResolver() {
        List<HandlerMethodArgumentResolver> argumentResolvers = mappingHandler.getArgumentResolvers();
        if (CollectionUtils.isEmpty(argumentResolvers)) {
            LOGGER.warn("Argument handlers in [{}] is empty, can't add [{}].", mappingHandler.getClass().getSimpleName(), GenericRequestBodyMethodProcessor.class.getSimpleName());
            return;
        }
        Integer index = null;
        RequestResponseBodyMethodProcessor processor = null;
        for (int i = 0; i < argumentResolvers.size(); i++) {
            HandlerMethodArgumentResolver argumentResolver = argumentResolvers.get(i);
            if (argumentResolver instanceof RequestResponseBodyMethodProcessor) {
                index = i;
                processor = (RequestResponseBodyMethodProcessor) argumentResolver;
                break;
            }
        }
        if (null == index) {
            LOGGER.warn("[{}] not exist in [{}] argument handlers, can't add [{}].", RequestResponseBodyMethodProcessor.class.getSimpleName(), mappingHandler.getClass().getSimpleName(), GenericRequestBodyMethodProcessor.class.getSimpleName());
            return;
        }
        List<HandlerMethodArgumentResolver> newResolvers = new ArrayList<>(argumentResolvers);
        newResolvers.set(index, new GenericRequestBodyMethodProcessor(processor));
        mappingHandler.setArgumentResolvers(newResolvers);
    }

    private static class GenericRequestBodyMethodProcessor extends RequestResponseBodyMethodProcessor {

        public GenericRequestBodyMethodProcessor(RequestResponseBodyMethodProcessor processor) {
            super(processor.messageConverters, List.of(new JsonViewRequestBodyAdvice()));
        }

        @Override
        protected Object readWithMessageConverters(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {
            HttpInputMessageReadable httpInputMessage = new HttpInputMessageReadable(inputMessage);
            try {
                return super.readWithMessageConverters(httpInputMessage, parameter, targetType);
            } catch (HttpMessageConversionException e) {
                return super.readWithMessageConverters(httpInputMessage, parameter, SimpleType.constructUnsafe(parameter.getParameterType()));
            }
        }
    }

    private static class HttpInputMessageReadable implements HttpInputMessage {
        private final byte[] bytes;
        private final HttpHeaders httpHeaders;

        private HttpInputMessageReadable(HttpInputMessage inputMessage) throws IOException {
            try (InputStream inputStream = inputMessage.getBody()) {
                this.bytes = IOUtils.toByteArray(inputStream);
            }
            this.httpHeaders = inputMessage.getHeaders();
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public HttpHeaders getHeaders() {
            return httpHeaders;
        }
    }
}
