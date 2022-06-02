package org.bardframework.crud.api.base;

import java.util.List;

public class PagedData<M> {

    private List<M> list;
    private long total;

    public PagedData() {
    }

    public PagedData(List<M> list, long total) {
        this.list = list;
        this.total = total;
    }

    public List<M> getList() {
        return list;
    }

    public void setList(List<M> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
