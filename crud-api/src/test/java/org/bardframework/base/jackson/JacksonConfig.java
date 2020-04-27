package org.bardframework.base.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class JacksonConfig {

    @Autowired
    public void configureObjectMapper(ObjectMapper objectMapper) {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(Page.class, PageImpl.class);

        SimpleModule module = new SimpleModule("Paging", Version.unknownVersion());
        module.setAbstractTypes(resolver);
        module.addDeserializer(Pageable.class, new PageableDeserializer());

        objectMapper.registerModule(module);

        objectMapper.addMixIn(PageImpl.class, DefaultPageMixin.class);
    }
}