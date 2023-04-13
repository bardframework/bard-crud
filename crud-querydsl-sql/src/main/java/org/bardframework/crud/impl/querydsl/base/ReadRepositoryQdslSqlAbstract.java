package org.bardframework.crud.impl.querydsl.base;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.bardframework.commons.utils.AssertionUtils;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.base.PagedData;
import org.bardframework.crud.api.base.ReadRepository;
import org.bardframework.crud.impl.querydsl.utils.QueryDslUtils;
import org.bardframework.form.model.filter.IdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by vahid on 1/17/17.
 */
public abstract class ReadRepositoryQdslSqlAbstract<M extends BaseModel<I>, C extends BaseCriteria<I>, I extends Serializable, U> implements ReadRepository<M, C, I, U> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final SQLQueryFactory queryFactory;
    protected final Class<M> modelClazz;
    protected final Class<C> criteriaClazz;
    protected final Class<I> idClazz;
    protected final Map<String, Path<?>> columns;

    public ReadRepositoryQdslSqlAbstract(SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
        this.modelClazz = ReflectionUtils.getGenericArgType(this.getClass(), 0);
        this.criteriaClazz = ReflectionUtils.getGenericArgType(this.getClass(), 1);
        this.idClazz = ReflectionUtils.getGenericArgType(this.getClass(), 2);
        this.columns = this.getEntity().getColumns().stream().collect(Collectors.toMap(path -> path.getMetadata().getName(), Function.identity()));
    }

    protected abstract Predicate getPredicate(C criteria, U user);

    protected abstract RelationalPathBase<?> getEntity();

    protected abstract Expression<M> getSelectExpression();

    protected abstract Expression<I> getIdSelectExpression();

    protected Predicate getPredicate(IdFilter<I> idFilter, U user) {
        if (!(this.getIdSelectExpression() instanceof SimpleExpression)) {
            throw new IllegalStateException("can't construct Predicate for IdFilter, getIdSelectExpression is not instance of SimpleExpression, override getPredicate(IdFilter, U) and implement it.");
        }
        return QueryDslUtils.getPredicate(idFilter, (SimpleExpression<I>) this.getIdSelectExpression());
    }


    @Transactional(readOnly = true)
    @Override
    public M get(I id, U user) {
        AssertionUtils.notNull(id, "Given id cannot be null.");
        C criteria = ReflectionUtils.newInstance(criteriaClazz);
        criteria.setIdFilter(new IdFilter<I>().setEquals(id));
        return this.getOne(criteria, user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<M> get(Collection<I> ids, U user) {
        AssertionUtils.notEmpty(ids, "Given ids cannot be empty.");
        C criteria = ReflectionUtils.newInstance(criteriaClazz);
        criteria.setIdFilter(new IdFilter<I>().setIn(ids));
        return this.get(criteria, user);
    }

    @Transactional(readOnly = true)
    @Override
    public PagedData<M> get(C criteria, Pageable pageable, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        AssertionUtils.notNull(pageable, "Given pageable cannot be null.");
        SQLQuery<?> query = this.prepareSelectQuery(criteria, user);
        this.setOrders(query, pageable.getSort());
        long total = query.fetchCount();
        if (0 >= total) {
            return new PagedData<>();
        }
        query = query.clone(this.getQueryFactory().getConnection());
        query.offset((long) (pageable.getPageNumber() - 1) * pageable.getPageSize());
        query.limit(pageable.getPageSize());
        List<M> result = query.select(this.getSelectExpression()).fetch();
        return new PagedData<>(result, total);
    }

    @Transactional(readOnly = true)
    @Override
    public long getCount(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        return this.prepareSelectQuery(criteria, user).fetchCount();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isExist(C criteria, U user) {
        return this.getCount(criteria, user) > 0;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isNotExist(C criteria, U user) {
        return this.getCount(criteria, user) == 0;
    }

    @Transactional(readOnly = true)
    @Override
    public List<I> getIds(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        return this.prepareSelectQuery(criteria, user).select(this.getIdSelectExpression()).fetch();
    }

    @Transactional(readOnly = true)
    @Override
    public List<M> get(C criteria, U user) {
        return this.get(criteria, (Sort) null, user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<M> get(C criteria, Sort sort, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null");
        SQLQuery<?> sqlQuery = this.prepareSelectQuery(criteria, user);
        this.setOrders(sqlQuery, sort);
        return sqlQuery.select(this.getSelectExpression()).fetch();
    }

    @Transactional(readOnly = true)
    @Override
    public M getOne(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null");
        return this.prepareSelectQuery(criteria, user).select(this.getSelectExpression()).fetchOne();
    }

    protected SQLQuery<?> prepareSelectQuery(C criteria, U user) {
        SQLQuery<?> query = this.getQueryFactory().query().from(this.getEntity());
        query.where(this.getPredicate(criteria.getIdFilter(), user));
        query.where(this.getPredicate(criteria, user));
        for (Class<?> clazz : this.getClass().getInterfaces()) {
            if (ReadExtendedRepositoryQdslSql.class.isAssignableFrom(clazz)) {
                ((ReadExtendedRepositoryQdslSql<C, I, U>) this).process(criteria, query, user);
            }
        }
        this.setSelectJoins(query, criteria, user);
        return query;
    }

    protected OrderSpecifier<?> toOrderSpecifier(Sort.Order order) {
        Path<?> path = this.getPath(order.getProperty());
        if (null == path) {
            log.warn("column not found for property [{}] to set order.", order.getProperty());
            return null;
        }
        if (path instanceof ComparableExpressionBase<?> column) {
            return order.isAscending() ? column.asc() : column.desc();
        }
        log.warn("column[{}] of property [{}] is not comparable, can't set order.", path.getClass().getSimpleName(), order.getProperty());
        return null;
    }

    protected Path<?> getPath(String columnName) {
        return columns.get(columnName);
    }

    protected void setSelectJoins(SQLQuery<?> query, C criteria, U user) {
    }

    protected void setOrders(SQLQuery<?> query, @Nullable Sort sort) {
        if (null == sort || sort.isEmpty()) {
            if (CollectionUtils.isNotEmpty(this.getDefaultOrders())) {
                query.orderBy(this.getDefaultOrders().toArray(new OrderSpecifier[0]));
            }
            return;
        }
        query.orderBy(sort.stream().map(this::toOrderSpecifier).filter(Objects::nonNull).toArray(OrderSpecifier[]::new));
    }

    protected SQLQueryFactory getQueryFactory() {
        return queryFactory;
    }

    protected List<OrderSpecifier<?>> getDefaultOrders() {
        return List.of();
    }
}
