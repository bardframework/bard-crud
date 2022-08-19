package org.bardframework.crud.api.base;

import org.bardframework.form.model.filter.IdFilter;

import java.io.Serializable;

/**
 * Created by vahid on 3/14/17.
 */
public abstract class BaseCriteriaAbstract<I extends Serializable> implements BaseCriteria<I> {

    protected IdFilter<I> idFilter;

    public BaseCriteriaAbstract() {
    }

    @Override
    public IdFilter<I> getIdFilter() {
        return idFilter;
    }

    @Override
    public void setIdFilter(IdFilter<I> idFilter) {
        this.idFilter = idFilter;
    }
}
