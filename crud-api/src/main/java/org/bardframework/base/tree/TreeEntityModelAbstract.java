package org.bardframework.base.tree;

import org.bardframework.base.crud.BaseModelAbstract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public abstract class TreeEntityModelAbstract<M extends BaseModelAbstract<I>, I extends Comparable<? super I>> extends BaseModelAbstract<I> implements TreeEntityModel<M> {

    protected M parent;

    protected List<M> children = new ArrayList<>();

    public TreeEntityModelAbstract() {
    }

    public TreeEntityModelAbstract(I id) {
        super(id);
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