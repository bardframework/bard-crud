package org.bardframework.crud.impl.querydsl.base;

import com.querydsl.core.dml.StoreClause;

/**
 * Created by vahid (va.zafari@gmail.com) on 10/22/17.
 */
public interface WriteExtendedRepositoryQdslSql<M, U> {

    <C extends StoreClause<C>> C process(C clause, M model, U user);
}
