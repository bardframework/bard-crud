package org.bardframework.crud.api.base;

import java.util.ArrayList;
import java.util.List;

public class PagedData<M> {

    private List<M> data;
    private long total;

    public PagedData() {
        this.data = new ArrayList<>();
    }

    public PagedData(List<M> data, long total) {
        this.data = data;
        this.total = total;
    }

    public List<M> getData() {
        return data;
    }

    public void setData(List<M> data) {
        this.data = data;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
