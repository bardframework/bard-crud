package org.bardframework.base.datatable;

import org.bardframework.commons.utils.AssertionUtils;

import java.util.List;

/**
 * Created by Vahid Zafari on 5/30/2016.
 */
public class DataTableData<TYPE> {

    private List<TYPE> list;
    private long total;

    public DataTableData() {
    }

    public DataTableData(List<TYPE> list, long total) {
        AssertionUtils.notNull(list, "null list not acceptable");
        this.list = list;
        this.total = total;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<TYPE> getList() {
        return list;
    }

    public void setList(List<TYPE> list) {
        this.list = list;
    }
}
