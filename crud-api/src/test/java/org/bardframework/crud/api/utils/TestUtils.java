package org.bardframework.crud.api.utils;

import org.springframework.data.domain.Pageable;

public class TestUtils {
    private TestUtils() {
        /*
            prevent instantiation
        */
    }

    public static String setPage(String url, Pageable pageable) {
        return String.format("%s?page=%d&size=%d", url, pageable.getPageNumber(), pageable.getPageSize());
    }
}
