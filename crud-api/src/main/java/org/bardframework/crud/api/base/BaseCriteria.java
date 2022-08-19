package org.bardframework.crud.api.base;

import org.bardframework.form.model.filter.IdFilter;

import java.io.Serializable;

/**
 * Created by vahid on 3/14/17.
 */
public interface BaseCriteria<I extends Serializable> {
    IdFilter<I> getIdFilter();

    void setIdFilter(IdFilter<I> idFilter);
}
