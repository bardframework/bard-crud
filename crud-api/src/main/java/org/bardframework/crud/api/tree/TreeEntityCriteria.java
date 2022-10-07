package org.bardframework.crud.api.tree;

import org.bardframework.form.model.filter.IdFilter;

import java.io.Serializable;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 * <p>
 * for isRoot set  getParentIdFilter()#setSpecified
 * for isLeaf set  getIdFilter()#setSpecified
 */
public interface TreeEntityCriteria<I extends Serializable> {

    Boolean getLeaf();

    IdFilter<I> getParentIdFilter();
}
