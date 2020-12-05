package org.bardframework.crud.api.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public class PageableDeserializer extends StdDeserializer<Pageable> {

    public PageableDeserializer() {
        this(null);
    }

    public PageableDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Pageable deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        int pageNumber = (int) node.get("pageNumber").numberValue();
        int pageSize = (int) node.get("pageSize").numberValue();
//        Sort sort = (Integer) ((IntNode) node.get("sort")).numberValue();

        return PageRequest.of(pageNumber, pageSize);
    }
}
