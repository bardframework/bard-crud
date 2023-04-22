package org.bardframework.crud.api.base;

import org.bardframework.form.model.filter.IdFilter;

/**
 * Created by vahid on 3/14/17.
 */
public interface BaseCriteria<I> {
    IdFilter<I> getIdFilter();

    void setIdFilter(IdFilter<I> idFilter);
}
