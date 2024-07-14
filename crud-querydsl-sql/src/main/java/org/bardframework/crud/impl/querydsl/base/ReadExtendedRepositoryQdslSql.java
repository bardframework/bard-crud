package org.bardframework.crud.impl.querydsl.base;

import com.querydsl.core.FetchableQuery;
import org.bardframework.crud.api.base.BaseCriteria;

/**
 * Created by vahid (va.zafari@gmail.com) on 10/22/17.
 */
public interface ReadExtendedRepositoryQdslSql<C extends BaseCriteria<I>, I, U> {

    void process(C criteria, FetchableQuery<?, ?> query, U user);

}
