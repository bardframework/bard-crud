package org.bardframework.crud.impl.querydsl.orderable;

import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.bardframework.commons.utils.AssertionUtils;
import org.bardframework.crud.api.base.BaseCriteriaAbstract;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.orderable.OrderableEntityRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

public interface OrderableEntityRepositoryQdslSql<M extends BaseModel<I>, C extends BaseCriteriaAbstract<I>, I extends Serializable & Comparable<? super I>, U> extends OrderableEntityRepository<M, C, I, U> {

    @Transactional(readOnly = true)
    @Override
    default M getFirst(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null");
        SQLQuery<M> query = this.getQueryFactory().select(this.getQBean()).from(this.getEntity());
        query = this.prepareQuery(query, criteria, user);
        query.orderBy(this.getOrderablePath().asc());
        return query.fetchFirst();
    }

    @Transactional(readOnly = true)
    @Override
    default M getLast(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null");
        SQLQuery<M> query = this.getQueryFactory().select(this.getQBean()).from(this.getEntity());
        query = this.prepareQuery(query, criteria, user);
        query.orderBy(this.getOrderablePath().desc());
        return query.fetchFirst();
    }


    SQLQueryFactory getQueryFactory();

    RelationalPathBase<?> getEntity();

    QBean<M> getQBean();

    <T> SQLQuery<T> prepareQuery(SQLQuery<T> query, C criteria, U user);

    ComparableExpressionBase<I> getOrderablePath();
}
