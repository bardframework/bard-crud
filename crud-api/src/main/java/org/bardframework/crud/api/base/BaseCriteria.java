package org.bardframework.crud.api.base;

import org.bardframework.crud.api.filter.IdFilter;

/**
 * Created by vahid on 3/14/17.
 */
public interface BaseCriteria<I extends Comparable<? super I>> {
    IdFilter<I> getId();

    void setId(IdFilter<I> idFilter);
}
