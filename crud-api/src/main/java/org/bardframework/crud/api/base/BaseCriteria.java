package org.bardframework.crud.api.base;

import org.bardframework.crud.api.filter.Filter;

/**
 * Created by vahid on 3/14/17.
 */
public interface BaseCriteria<I extends Comparable<? super I>> {
    Filter<I> getId();

    void setId(Filter<I> filter);
}
