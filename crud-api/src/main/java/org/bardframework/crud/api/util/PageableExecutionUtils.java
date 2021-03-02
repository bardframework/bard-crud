package org.bardframework.crud.api.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.function.LongSupplier;

public final class PageableExecutionUtils {
    private PageableExecutionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> Page<T> getPage(List<T> content, Pageable pageable, LongSupplier totalSupplier) {
        Assert.notNull(content, "Content must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");
        Assert.notNull(totalSupplier, "TotalSupplier must not be null!");
        if (!pageable.isUnpaged() && pageable.getOffset() != 0L) {
            return content.size() != 0 && pageable.getPageSize() > content.size() ? new PageImpl<>(content, pageable, pageable.getOffset() + (long) content.size()) : new PageImpl<>(content, pageable, totalSupplier.getAsLong());
        } else {
            return !pageable.isUnpaged() && pageable.getPageSize() <= content.size() ? new PageImpl<>(content, pageable, totalSupplier.getAsLong()) : new PageImpl<>(content, pageable, content.size());
        }
    }
}
