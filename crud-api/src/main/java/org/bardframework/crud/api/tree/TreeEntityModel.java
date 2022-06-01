package org.bardframework.crud.api.tree;

import org.bardframework.crud.api.base.BaseModel;

import java.util.List;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public interface TreeEntityModel<M extends BaseModel<?>> {

    M getParent();

    void setParent(M parent);

    List<M> getChildren();

    void setChildren(List<M> roots);

    void addChild(M child);

    default boolean isRoot() {
        return null == this.getParent();
    }
}
