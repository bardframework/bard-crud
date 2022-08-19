package org.bardframework.crud.api.tree;

import java.io.Serializable;
import java.util.List;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public interface TreeEntityCriteria<I extends Serializable> {

    Boolean getLeaf();

    Boolean getRoot();

    List<I> getParentIds();
}
