package org.bardframework.crud.api.filter;

import io.github.jhipster.service.filter.Filter;

public class IdFilter<I> extends Filter<I> {

    public IdFilter() {
    }

    public IdFilter(IdFilter<I> filter) {
        super(filter);
    }

    @Override
    public IdFilter<I> copy() {
        return new IdFilter<>(this);
    }
}
