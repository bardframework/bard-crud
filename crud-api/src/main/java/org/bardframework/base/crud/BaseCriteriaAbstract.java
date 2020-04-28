package org.bardframework.base.crud;

import org.bardframework.base.filter.IdFilter;

/**
 * Created by vahid on 3/14/17.
 */
public abstract class BaseCriteriaAbstract<I extends Comparable<? super I>> implements BaseCriteria<I> {

    protected IdFilter<I> id;

    public BaseCriteriaAbstract() {
    }

    @Override
    public IdFilter<I> getId() {
        return id;
    }

    public void setId(IdFilter<I> id) {
        this.id = id;
    }
}