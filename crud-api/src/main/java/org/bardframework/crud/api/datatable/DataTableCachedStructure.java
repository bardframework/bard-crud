package org.bardframework.crud.api.datatable;

/**
 * Created by Vahid Zafari on 4/11/2017.
 */
public class DataTableCachedStructure extends DataTableFilter {

    private String table;

    public DataTableCachedStructure() {
    }

    public DataTableCachedStructure(String table) {
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
