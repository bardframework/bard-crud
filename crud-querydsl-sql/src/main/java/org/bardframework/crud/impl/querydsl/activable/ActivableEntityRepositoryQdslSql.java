package org.bardframework.crud.impl.querydsl.activable;

import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.bardframework.crud.api.activable.ActivableEntityCriteria;
import org.bardframework.crud.api.activable.ActivableEntityRepository;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.impl.querydsl.base.ReadExtendedRepositoryQdslSql;
import org.springframework.transaction.annotation.Transactional;

public interface ActivableEntityRepositoryQdslSql<C extends BaseCriteria<I> & ActivableEntityCriteria, I extends Comparable<? super I>, U> extends ActivableEntityRepository<I, U>, ReadExtendedRepositoryQdslSql<C, I, U> {

    @Transactional
    @Override
    default boolean setEnable(I id, boolean enable, U user) {
        return this.getQueryFactory().update(this.getEntity()).set(this.getEnablePath(), enable).where(this.getIdentifierPath().eq(id)).execute() == 1;
    }

    SQLQueryFactory getQueryFactory();

    RelationalPathBase<?> getEntity();

    SimpleExpression<I> getIdentifierPath();

    BooleanPath getEnablePath();

    @Override
    default <T> SQLQuery<T> process(C criteria, SQLQuery<T> query, U user) {
        if (null != criteria.getEnable()) {
            query.where(this.getEnablePath().eq(criteria.getEnable()));
        }
        return query;
    }
}
