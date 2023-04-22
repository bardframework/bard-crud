package org.bardframework.crud.impl.querydsl.base;

import com.querydsl.sql.SQLQuery;
import org.bardframework.crud.api.base.BaseCriteria;

/**
 * Created by vahid (va.zafari@gmail.com) on 10/22/17.
 */
public interface ReadExtendedRepositoryQdslSql<C extends BaseCriteria<I>, I, U> {

    <T> SQLQuery<T> process(C criteria, SQLQuery<T> query, U user);

}
