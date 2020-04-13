package org.bardframework.base.tree;

import org.bardframework.base.crud.BaseModelAbstract;

import java.util.List;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public interface TreeEntityModel<M extends BaseModelAbstract<?>> {

    M getParent();

    void setParent(M parent);

    List<M> getChildren();

    void setChildren(List<M> roots);

    void addChild(M child);

    default boolean isRoot() {
        return null == this.getParent();
    }
}