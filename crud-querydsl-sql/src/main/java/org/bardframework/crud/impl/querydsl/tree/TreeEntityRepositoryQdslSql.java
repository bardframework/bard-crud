package org.bardframework.crud.impl.querydsl.tree;

import com.querydsl.core.FetchableQuery;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.tree.TreeEntityCriteria;
import org.bardframework.crud.api.tree.TreeEntityModel;
import org.bardframework.crud.api.tree.TreeEntityRepository;
import org.bardframework.crud.impl.querydsl.base.ReadExtendedRepositoryQdslSql;
import org.bardframework.crud.impl.querydsl.utils.QueryDslUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public interface TreeEntityRepositoryQdslSql<M extends BaseModel<I> & TreeEntityModel<M>, C extends BaseCriteria<I> & TreeEntityCriteria<I>, I, U> extends TreeEntityRepository<M, I, U>, ReadExtendedRepositoryQdslSql<C, I, U> {

    SimpleExpression<I> getParentIdSelectExpression();

    SimpleExpression<I> getIdSelectExpression();

    RelationalPathBase<?> getEntity();

    Expression<M> getSelectExpression();

    SQLQueryFactory getQueryFactory();

    @Override
    default void process(C criteria, FetchableQuery<?, ?> query, U user) {
        query.where(QueryDslUtils.getPredicate(criteria.getParentIdFilter(), this.getParentIdSelectExpression()));
        if (null != criteria.getLeaf()) {
            SQLQuery<I> parentIdsQuery = SQLExpressions.select(this.getParentIdSelectExpression()).from(this.getEntity()).where(this.getParentIdSelectExpression().isNotNull());
            if (criteria.getLeaf()) {
                query.where(this.getIdSelectExpression().notIn(parentIdsQuery));
            } else {
                query.where(this.getIdSelectExpression().in(parentIdsQuery));
            }
        }
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
