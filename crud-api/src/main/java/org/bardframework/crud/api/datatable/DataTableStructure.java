package org.bardframework.crud.api.datatable;

import java.util.List;

/**
 * Created by Vahid Zafari on 5/12/2016.
 */
public class DataTableStructure {

    private final Class<?> clazz;
    private final List<HeaderAbstract> headers;

    public DataTableStructure(Class<?> clazz, List<HeaderAbstract> headers) {
        this.clazz = clazz;
        this.headers = headers;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<HeaderAbstract> getHeaders() {
        return headers;
    }
}
