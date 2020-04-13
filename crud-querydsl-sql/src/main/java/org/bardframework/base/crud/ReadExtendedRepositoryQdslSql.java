package org.bardframework.base.crud;

import com.querydsl.sql.SQLQuery;

import java.io.Serializable;

/**
 * Created by vahid (va.zafari@gmail.com) on 10/22/17.
 */
public interface ReadExtendedRepositoryQdslSql<C extends BaseCriteria<I>, I extends Serializable, U> {

    <T> SQLQuery<T> process(C criteria, SQLQuery<T> query, U user);

}
