package org.bardframework.crud.common.converter;

import org.springframework.core.convert.converter.Converter;

/**
 * Created by Vahid Zafari on 1/29/2016.
 */
public class NoOpConverter implements Converter<Object, Object> {

    @Override
    public Object convert(Object value) {
        return value;
    }
}
