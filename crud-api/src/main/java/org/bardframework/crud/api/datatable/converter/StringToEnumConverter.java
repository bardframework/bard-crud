package org.bardframework.crud.api.datatable.converter;

import org.bardframework.commons.utils.AssertionUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * Created by Vahid Zafari on 1/29/2016.
 */
public class StringToEnumConverter<ENUM extends Enum<ENUM>> implements Converter<String, ENUM> {

    private Class<ENUM> enumClazz;

    public StringToEnumConverter() {
    }

    public StringToEnumConverter(Class<ENUM> enumClazz) {
        AssertionUtils.notNull(enumClazz, "null class not acceptable");
        this.enumClazz = enumClazz;
    }

    public Class<ENUM> getEnumClazz() {
        return enumClazz;
    }

    public void setEnumClazz(Class<ENUM> enumClazz) {
        this.enumClazz = enumClazz;
    }

    @Override
    public ENUM convert(String value) {
        return Enum.valueOf(enumClazz, value);
    }
}
