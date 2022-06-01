package org.bardframework.crud.api.tree;

import org.bardframework.crud.api.base.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public abstract class TreeEntityModelAbstract<M extends BaseModel<I>, I extends Comparable<? super I>> implements TreeEntityModel<M>, BaseModel<I> {

    protected M parent;

    protected List<M> children = new ArrayList<>();

    public TreeEntityModelAbstract() {
    }

    public TreeEntityModelAbstract(M parent) {
        this.parent = parent;
    }

    @Override
    public void addChild(M child) {
        if (null == this.children) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }

    @Override
    public M getParent() {
        return parent;
    }

    @Override
    public void setParent(M parent) {
        this.parent = parent;
    }

    @Override
    public List<M> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<M> children) {
        this.children = children;
    }
}
