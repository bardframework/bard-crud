package org.bardframework.base.datatable;

import com.querydsl.sql.mssql.SQLServerQuery;

import java.util.Collection;

/**
 * Created by Vahid Zafari on 6/18/2017.
 */
public interface DataLimitation<E extends Enum<E> & DataLimitation<E>> {

    <R> SQLServerQuery<R> setRestrictions(SQLServerQuery<R> query, Collection<E> enumz);
}
