package org.bardframework.base.searchable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bardframework.commons.jackson.converter.NormalPersianCharacterDeserializer;

public interface SearchableCriteria {

    @JsonDeserialize(using = NormalPersianCharacterDeserializer.class)
    String getQuery();
}