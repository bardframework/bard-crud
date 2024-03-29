package org.bardframework.crud.api.tree;

import org.bardframework.form.model.filter.IdFilter;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 * <p>
 * for isRoot set  getParentIdFilter()#setSpecified
 * for isLeaf set  getIdFilter()#setSpecified
 */
public interface TreeEntityCriteria<I> {

    Boolean getLeaf();

    IdFilter<I> getParentIdFilter();
}
