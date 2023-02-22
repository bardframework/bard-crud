package org.bardframework.crud.api.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Pageable;

@UtilityClass
public class TestUtils {

    public static String setPage(String url, Pageable pageable) {
        return String.format("%s?page=%d&size=%d", url, pageable.getPageNumber(), pageable.getPageSize());
    }
}
