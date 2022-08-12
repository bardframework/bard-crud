package org.bardframework.crud.impl.querydsl.base;

import com.querydsl.core.dml.StoreClause;

/**
 * Created by vahid (va.zafari@gmail.com) on 10/22/17.
 */
public interface UpdateExtendedRepositoryQdslSql<M, U> {

    <C extends StoreClause<C>> C onUpdate(C clause, M model, U user);
}
