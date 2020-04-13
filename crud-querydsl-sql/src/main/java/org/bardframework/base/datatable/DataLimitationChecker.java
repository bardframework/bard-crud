package org.bardframework.base.datatable;

import com.querydsl.sql.mssql.SQLServerQuery;
import org.bardframework.commons.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vahid Zafari on 6/3/2016.
 */
public class DataLimitationChecker<T extends Enum<T> & DataLimitation<T>> {

    private final Class<T> clazz;

    public DataLimitationChecker(Class<T> clazz) {
        this.clazz = clazz;
    }

    public <R> SQLServerQuery<R> setRestriction(SQLServerQuery<R> query, List<String> selected) {
        List<T> enumz = new ArrayList<>();
        for (String enumId : selected) {
            enumz.add(Enum.valueOf(clazz, enumId.replace(clazz.getSimpleName() + ".", StringUtils.EMPTY)));
        }
        return enumz.get(0).setRestrictions(query, enumz);
    }
}
