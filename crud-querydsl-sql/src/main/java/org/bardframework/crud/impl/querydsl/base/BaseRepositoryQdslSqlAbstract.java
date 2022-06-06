package org.bardframework.crud.impl.querydsl.base;

import com.querydsl.core.dml.StoreClause;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import org.apache.commons.collections4.CollectionUtils;
import org.bardframework.commons.utils.AssertionUtils;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.base.BaseRepository;
import org.bardframework.crud.api.base.PagedData;
import org.bardframework.crud.api.filter.Filter;
import org.bardframework.crud.impl.querydsl.utils.QueryDslUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by vahid on 1/17/17.
 */
public abstract class BaseRepositoryQdslSqlAbstract<M extends BaseModel<I>, C extends BaseCriteria<I>, I extends Comparable<? super I>, U> implements BaseRepository<M, C, I, U> {

    private static final int DEFAULT_SIZE = 20;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected final Class<M> modelClazz;
    protected final Class<C> criteriaClazz;
    private final SQLQueryFactory queryFactory;

    public BaseRepositoryQdslSqlAbstract(SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
        this.modelClazz = ReflectionUtils.getGenericArgType(this.getClass(), 0);
        this.criteriaClazz = ReflectionUtils.getGenericArgType(this.getClass(), 1);
    }

    protected abstract void setCriteria(C criteria, SQLQuery<?> query, U user);

    protected abstract RelationalPathBase<?> getEntity();

    protected abstract QBean<M> getQBean();

    protected abstract <T extends StoreClause<T>> void toClause(T clause, M model, U user);

    protected abstract void setIdentifier(M entity, U user);

    protected void setIdentifier(SQLInsertClause clause, I identifier, U user) {
        clause.set(this.getIdentifierPath(), identifier);
    }

    protected void setIdentifier(SQLUpdateClause clause, I identifier, U user) {
        clause.where(this.getIdentifierPath().eq(identifier));
    }

    protected <T extends StoreClause<T>> void fillClause(T clause, M model, U user) {
        this.toClause(clause, model, user);
        for (Class<?> clazz : this.getClass().getInterfaces()) {
            if (WriteExtendedRepositoryQdslSql.class.isAssignableFrom(clazz)) {
                ((WriteExtendedRepositoryQdslSql) this).process(clause, model, user);
            }
        }
    }

    @Transactional
    @Override
    public M save(M model, U user) {
        AssertionUtils.notNull(model, "Given model cannot be null.");
        return this.save(Collections.singletonList(model), user).get(0);
    }

    /**
     * @return not changed models if models is null or empty, saved models otherwise.
     */
    @Transactional
    @Override
    public List<M> save(List<M> models, U user) {
        AssertionUtils.notNull(models, "Given models cannot be null.");
        if (CollectionUtils.isEmpty(models)) {
            return Collections.emptyList();
        }
        SQLInsertClause insertClause = this.getQueryFactory().insert(this.getEntity());
        models.forEach(model -> {
                    this.fillClause(insertClause, model, user);
                    this.setIdentifier(model, user);
                    this.setIdentifier(insertClause, model.getId(), user);
                    insertClause.addBatch();
                }
        );
        long affectedRowsCount = insertClause.execute();
        if (models.size() != affectedRowsCount) {
            LOGGER.warn("expect insert '{}' row, but '{}' row(s) inserted.", models.size(), affectedRowsCount);
        }
        return models;
    }

    @Transactional
    @Override
    public M update(M model, U user) {
        AssertionUtils.notNull(model, "model cannot be null.");
        return this.update(Collections.singletonList(model), user).get(0);
    }

    @Transactional
    @Override
    public List<M> update(List<M> models, U user) {
        AssertionUtils.notNull(models, "Given models cannot be null.");
        if (CollectionUtils.isEmpty(models)) {
            return Collections.emptyList();
        }
        SQLUpdateClause updateClause = this.getQueryFactory().update(this.getEntity());
        models.forEach(model ->
                {
                    AssertionUtils.notNull(model.getId(), "model identifier is not provided, can't update");
                    this.setIdentifier(updateClause, model.getId(), user);
                    this.fillClause(updateClause, model, user);
                    updateClause.addBatch();
                }
        );
        long affectedRowsCount = updateClause.execute();
        if (models.size() != affectedRowsCount) {
            LOGGER.error("expect update '{}' row, but '{}' row(s) updated.", models.size(), affectedRowsCount);
            throw new IllegalStateException("affected rows in update not valid");
        }
        return models;
    }

    @Transactional
    @Override
    public M patch(I id, Map<String, Object> patch, U user) {
        AssertionUtils.notNull(id, "id cannot be null.");
        AssertionUtils.notEmpty(patch, "patch cannot be empty.");
        final SQLUpdateClause updateClause = this.getQueryFactory().update(this.getEntity());
        this.setIdentifier(updateClause, id, user);
        List<Path<?>> columns = this.getEntity().getColumns();
        for (Path<?> column : columns) {
            String property = column.getMetadata().getName();
            if (patch.containsKey(property)) {
                Object value = patch.get(property);
                if (null == value) {
                    updateClause.setNull(column);
                } else {
                    updateClause.set((Path<Object>) column, value);
                }
            }
        }

        long affectedRowsCount = updateClause.execute();
        if (1 != affectedRowsCount) {
            throw new IllegalStateException("expect affect one row, but " + affectedRowsCount + " row(s) updated.");
        }
        return this.get(id, user);
    }

    protected abstract <T extends ComparableExpression<I>> T getIdentifierPath();

    @Transactional(readOnly = true)
    @Override
    public M get(I identifier, U user) {
        AssertionUtils.notNull(identifier, "Given Identifier cannot be null.");
        C criteria = ReflectionUtils.newInstance(criteriaClazz);
        criteria.setId(new Filter<I, Filter<I, ?>>().setEquals(identifier));
        return this.getOne(criteria, user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<M> get(List<I> ids, U user) {
        AssertionUtils.notEmpty(ids, "Given Identifiers cannot be empty.");
        C criteria = ReflectionUtils.newInstance(criteriaClazz);
        criteria.setId(new Filter<I, Filter<I, ?>>().setIn(ids));
        return this.get(criteria, user);
    }

    protected SQLQuery<?> prepareQuery(C criteria, @Nullable Sort sort, U user) {
        SQLQuery<?> query = this.getQueryFactory().query().from(this.getEntity());
        this.setJoins(query, user);
        this.setCriteria(criteria, query, user);
        QueryDslUtils.applyFilter(query, criteria.getId(), this.getIdentifierPath());
        for (Class<?> clazz : this.getClass().getInterfaces()) {
            if (ReadExtendedRepositoryQdslSql.class.isAssignableFrom(clazz)) {
                ((ReadExtendedRepositoryQdslSql<C, I, U>) this).process(criteria, query, user);
            }
        }
        this.setOrders(query, sort);
        return query;
    }

    @Transactional(readOnly = true)
    @Override
    public PagedData<M> get(C criteria, Pageable pageable, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        AssertionUtils.notNull(pageable, "Given pageable cannot be null.");
        SQLQuery<?> query = this.prepareQuery(criteria, pageable.getSort(), user);
        long total = query.fetchCount();
        if (0 >= total) {
            return new PagedData<>();
        }
        query = query.clone(this.getQueryFactory().getConnection());
        if (pageable.getPageSize() == 0) {
            query.offset((long) (Math.max(pageable.getPageNumber(), 0)) * DEFAULT_SIZE);
            query.limit(DEFAULT_SIZE);
        } else {
            query.offset(pageable.getOffset());
            query.limit(pageable.getPageSize());
        }
        List<M> result = query.select(this.getQBean()).fetch();
        return new PagedData<>(result, total);
    }

    @Transactional(readOnly = true)
    @Override
    public long getCount(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        return this.prepareQuery(criteria, null, user).fetchCount();
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

    @Transactional
    @Override
    public long delete(I id, U user) {
        AssertionUtils.notNull(id, "id should not be null.");
        return this.delete(Collections.singletonList(id), user);
    }

    @Transactional
    @Override
    public long delete(List<I> ids, U user) {
        AssertionUtils.notEmpty(ids, "Given ids cannot be null.");
        C criteria = ReflectionUtils.newInstance(criteriaClazz);
        criteria.setId(new Filter<I, Filter<I, ?>>().setIn(ids));

        return this.delete(criteria, user);
    }

    @Transactional
    @Override
    public long delete(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Criteria object should not be null.");
        return this.getQueryFactory().delete(this.getEntity())
                .where(this.getIdentifierPath().in(this.getIds(criteria, user)))
                .execute();
    }

    /**
     * Does not use criteria to delete (User access may not be controlled)
     */
    @Transactional
    @Override
    public long directDelete(List<I> ids, U user) {
        AssertionUtils.notEmpty(ids, "ids should not be empty.");
        return this.getQueryFactory().delete(this.getEntity())
                .where(this.getIdentifierPath().in(ids))
                .execute();
    }

    @Transactional(readOnly = true)
    @Override
    public List<I> getIds(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        return this.prepareQuery(criteria, null, user).select(this.getIdentifierPath()).fetch();
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
        return this.prepareQuery(criteria, sort, user).select(this.getQBean()).fetch();
    }

    @Transactional(readOnly = true)
    @Override
    public M getOne(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null");
        return this.prepareQuery(criteria, null, user).select(this.getQBean()).fetchOne();
    }

    protected void setJoins(SQLQuery<?> query, U user) {
    }

    protected void setOrders(SQLQuery<?> query, @Nullable Sort sort) {
        List<OrderSpecifier<?>> list;
        Map<String, OrderField> map = this.getSortColumns();

        if (sort == null || sort.isUnsorted()) {
            list = map.values().stream().map(OrderField::getColumnDirection).collect(Collectors.toList());
        } else {
            list = sort.stream()
                    .filter(b -> map.containsKey(b.getProperty()))
                    .map(b -> {
                        if (b.isAscending()) {
                            return map.get(b.getProperty()).getColumn().asc();
                        }
                        return map.get(b.getProperty()).getColumn().desc();
                    }).collect(Collectors.toList());
        }
        query.orderBy(list.toArray(new OrderSpecifier[0]));
    }

    protected SQLQueryFactory getQueryFactory() {
        return queryFactory;
    }

    protected Map<String, OrderField> getSortColumns() {
        return Map.of();
    }
}
