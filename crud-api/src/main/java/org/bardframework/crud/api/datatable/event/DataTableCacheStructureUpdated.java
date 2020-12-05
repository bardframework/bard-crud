package org.bardframework.crud.api.datatable.event;

/**
 * Created by Vahid Zafari on 4/21/2017.
 */
public class DataTableCacheStructureUpdated {

    private final String userId;

    public DataTableCacheStructureUpdated(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
