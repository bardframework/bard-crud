package org.bardframework.base.crud;

import org.bardframework.base.filter.IdFilter;

/**
 * Created by vahid on 3/14/17.
 */
public interface BaseCriteria<I extends Comparable<? super I>> {
    IdFilter<I> getId();
}