package org.bardframework.crud.impl.querydsl.tree;

import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModelAbstract;
import org.bardframework.crud.api.tree.TreeEntityCriteria;
import org.bardframework.crud.api.tree.TreeEntityModel;
import org.bardframework.crud.api.tree.TreeEntityRepository;
import org.bardframework.crud.impl.querydsl.base.ReadExtendedRepositoryQdslSql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public interface TreeEntityRepositoryQdslSql<M extends BaseModelAbstract<I> & TreeEntityModel<M>, C extends BaseCriteria<I> & TreeEntityCriteria<I>, I extends Comparable<? super I>, U> extends TreeEntityRepository<M, I, U>, ReadExtendedRepositoryQdslSql<C, I, U> {

    <T extends SimpleExpression<I>> T getParentPath();

    <T extends SimpleExpression<I>> T getIdentifierPath();

    RelationalPathBase<?> getEntity();

    QBean<M> getQBean();

    @Override
    default <T> SQLQuery<T> process(C criteria, SQLQuery<T> query, U user) {
        if (null != criteria.getParentIds()) {
            query.where(this.getParentPath().in(criteria.getParentIds()));
        }
        if (null != criteria.getRoot()) {
            query.where(criteria.getRoot() ? this.getParentPath().isNull() : this.getParentPath().isNotNull());
        }
        if (null != criteria.getLeaf()) {
            SQLQuery<I> parentIdsQuery = SQLExpressions.select(this.getParentPath()).from(this.getEntity()).where(this.getParentPath().isNotNull());
            query.where(criteria.getLeaf() ? this.getIdentifierPath().notIn(parentIdsQuery) : this.getIdentifierPath().in(parentIdsQuery));
        }
        return query;
    }

    @Transactional(readOnly = true)
    @Override
    default List<M> getWithChildren(I id, U user) {
//        SQLQuery<M> query = this.getQueryFactory().select(this.getQBean()).from(this.getEntity());
//        query = this.setJoins(query);
//        return query.startWith(this.getIdentifierPath().eq(id))
//                .connectByPrior(this.getIdentifierPath().eq(this.getParentPath()))
//                .orderSiblingsBy(this.getIdentifierPath())
//                .fetch();
        return null;
    }
}
