package org.bardframework.crud.api.filter;

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
