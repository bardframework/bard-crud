package org.bardframework.crud.api.base;

import org.bardframework.crud.api.filter.Filter;

/**
 * Created by vahid on 3/14/17.
 */
public abstract class BaseCriteriaAbstract<I extends Comparable<? super I>> implements BaseCriteria<I> {

    protected Filter<I, ?> id;

    public BaseCriteriaAbstract() {
    }

    @Override
    public Filter<I, ?> getId() {
        return id;
    }

    @Override
    public void setId(Filter<I, ?> filter) {
        this.id = filter;
    }
}
