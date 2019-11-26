package org.bardframework.base.crud;

import java.io.Serializable;
import java.util.List;

/**
 * Created by vahid on 3/14/17.
 */
public abstract class BaseCriteriaAbstract<I extends Serializable> implements BaseCriteria<I> {

    protected long page;
    protected long size;
    protected List<I> excludes;
    protected List<I> ids;

    public BaseCriteriaAbstract() {
    }

    public BaseCriteriaAbstract(long page, long size) {
        this.page = page;
        this.size = size;
    }

    public BaseCriteriaAbstract(List<I> ids) {
        this.ids = ids;
    }

    @Override
    public final long getPage() {
        return page;
    }

    public final void setPage(long page) {
        this.page = page;
    }

    @Override
    public final long getSize() {
        return size;
    }

    public final void setSize(long size) {
        this.size = size;
    }

    @Override
    public List<I> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<I> excludes) {
        this.excludes = excludes;
    }

    @Override
    public List<I> getIds() {
        return ids;
    }

    public void setIds(List<I> ids) {
        this.ids = ids;
    }
}
