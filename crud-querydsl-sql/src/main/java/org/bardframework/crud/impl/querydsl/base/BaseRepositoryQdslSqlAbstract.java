package org.bardframework.crud.impl.querydsl.base;

import com.querydsl.core.dml.StoreClause;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import org.apache.commons.collections4.CollectionUtils;
import org.bardframework.commons.utils.AssertionUtils;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.base.BaseRepository;
import org.bardframework.crud.api.base.PagedData;
import org.bardframework.crud.impl.querydsl.utils.QueryDslUtils;
import org.bardframework.form.model.filter.IdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;

/**
 * Created by vahid on 1/17/17.
 */
public abstract class BaseRepositoryQdslSqlAbstract<M extends BaseModel<I>, C extends BaseCriteria<I>, I extends Serializable, U> implements BaseRepository<M, C, I, U> {

    private static final int DEFAULT_SIZE = 20;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected final Class<M> modelClazz;
    protected final Class<C> criteriaClazz;
    protected final Class<I> idClazz;
    private final SQLQueryFactory queryFactory;

    public BaseRepositoryQdslSqlAbstract(SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
        this.modelClazz = ReflectionUtils.getGenericArgType(this.getClass(), 0);
        this.criteriaClazz = ReflectionUtils.getGenericArgType(this.getClass(), 1);
        this.idClazz = ReflectionUtils.getGenericArgType(this.getClass(), 2);
    }

    protected abstract <T extends StoreClause<T>> void onSave(T clause, M model, U user);

    protected abstract <T extends StoreClause<T>> void onUpdate(T clause, M model, U user);

    protected abstract Predicate getPredicate(C criteria, U user);

    protected abstract RelationalPathBase<?> getEntity();

    protected abstract Expression<M> getSelectExpression();

    protected abstract Expression<I> getIdSelectExpression();

    protected abstract I generateId(M entity, U user);

    protected Predicate getPredicate(IdFilter<I> idFilter, U user) {
        if (!(this.getIdSelectExpression() instanceof SimpleExpression)) {
            throw new IllegalStateException("can't construct Predicate for IdFilter, getIdSelectExpression is not instance of SimpleExpression, override getPredicate(IdFilter, U) and implement it.");
        }
        return QueryDslUtils.getPredicate(idFilter, (SimpleExpression<I>) this.getIdSelectExpression());
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
    public List<M> save(Collection<M> models, U user) {
        AssertionUtils.notNull(models, "Given models cannot be null.");
        if (CollectionUtils.isEmpty(models)) {
            return Collections.emptyList();
        }
        List<M> list = new ArrayList<>(models);
        SQLInsertClause insertClause = this.getQueryFactory().insert(this.getEntity());
        list.forEach(model -> {
                    model.setId(this.generateId(model, user));
                    this.onSaveInternal(insertClause, model, user);
                    insertClause.addBatch();
                }
        );
        Long affectedCount;
        if (this.getIdSelectExpression() instanceof Path) {
            List<I> generatedIds = insertClause.executeWithKeys((Path<I>) this.getIdSelectExpression());
            if (CollectionUtils.isNotEmpty(generatedIds)) {
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).setId(generatedIds.get(i));
                }
            }
            /*
                در حالتی که شناسه ها در دیتابیس تولید نمی شوند؛ این لیست خالی است.
             */
            affectedCount = CollectionUtils.isNotEmpty(generatedIds) ? (long) generatedIds.size() : null;
        } else {
            affectedCount = insertClause.execute();
        }
        if (null != affectedCount && list.size() != affectedCount) {
            LOGGER.warn("expect insert '{}' row, but '{}' row(s) inserted.", list.size(), affectedCount);
        }
        return list;
    }

    @Transactional
    @Override
    public M update(M model, U user) {
        AssertionUtils.notNull(model, "model cannot be null.");
        return this.update(Collections.singletonList(model), user).get(0);
    }

    @Transactional
    @Override
    public List<M> update(Collection<M> models, U user) {
        AssertionUtils.notNull(models, "Given models cannot be null.");
        if (CollectionUtils.isEmpty(models)) {
            return Collections.emptyList();
        }
        SQLUpdateClause updateClause = this.getQueryFactory().update(this.getEntity());
        models.forEach(model ->
                {
                    AssertionUtils.notNull(model.getId(), "model identifier is not provided, can't update");
                    updateClause.where(this.getPredicate(new IdFilter<I>().setEquals(model.getId()), user));
                    this.onUpdateInternal(updateClause, model, user);
                    updateClause.addBatch();
                }
        );
        long affectedRowsCount = updateClause.execute();
        if (models.size() != affectedRowsCount) {
            LOGGER.error("expect update '{}' row, but '{}' row(s) updated.", models.size(), affectedRowsCount);
            throw new IllegalStateException("affected rows in update not valid");
        }
        return new ArrayList<>(models);
    }

    @Transactional
    @Override
    public M patch(I id, Map<String, Object> patch, U user) {
        AssertionUtils.notNull(id, "id cannot be null.");
        AssertionUtils.notEmpty(patch, "patch cannot be empty.");
        final SQLUpdateClause updateClause = this.getQueryFactory().update(this.getEntity());
        updateClause.where(this.getPredicate(new IdFilter<I>().setEquals(id), user));
        for (Map.Entry<String, Object> entry : patch.entrySet()) {
            Path<Object> path = (Path<Object>) this.getPath(entry.getKey());
            if (null == entry.getValue()) {
                updateClause.setNull(path);
            } else {
                updateClause.set(path, entry.getValue());
            }
        }

        long affectedRowsCount = updateClause.execute();
        if (1 != affectedRowsCount) {
            throw new IllegalStateException("expect affect one row, but " + affectedRowsCount + " row(s) updated.");
        }
        return this.get(id, user);
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
        if (pageable.getPageSize() == 0) {
            query.offset((long) (Math.max(pageable.getPageNumber(), 0)) * DEFAULT_SIZE);
            query.limit(DEFAULT_SIZE);
        } else {
            query.offset(pageable.getOffset());
            query.limit(pageable.getPageSize());
        }
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

    @Transactional
    @Override
    public long delete(I id, U user) {
        AssertionUtils.notNull(id, "id should not be null.");
        return this.delete(Collections.singletonList(id), user);
    }

    @Transactional
    @Override
    public long delete(Collection<I> ids, U user) {
        AssertionUtils.notEmpty(ids, "Given ids cannot be null.");
        C criteria = ReflectionUtils.newInstance(criteriaClazz);
        criteria.setIdFilter(new IdFilter<I>().setIn(ids));
        return this.delete(criteria, user);
    }

    @Transactional
    @Override
    public long delete(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Criteria object should not be null.");
        SQLDeleteClause deleteClause = this.getQueryFactory().delete(this.getEntity());
        deleteClause.where(this.getPredicate(criteria.getIdFilter(), user));
        deleteClause.where(this.getPredicate(criteria, user));
        return deleteClause.execute();
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

    private <T extends StoreClause<T>> void onSaveInternal(T clause, M model, U user) {
        this.onSave(clause, model, user);
        for (Class<?> clazz : this.getClass().getInterfaces()) {
            if (SaveExtendedRepositoryQdslSql.class.isAssignableFrom(clazz)) {
                ((SaveExtendedRepositoryQdslSql) this).onSave(clause, model, user);
            }
        }
    }

    private <T extends StoreClause<T>> void onUpdateInternal(T clause, M model, U user) {
        this.onUpdate(clause, model, user);
        for (Class<?> clazz : this.getClass().getInterfaces()) {
            if (UpdateExtendedRepositoryQdslSql.class.isAssignableFrom(clazz)) {
                ((UpdateExtendedRepositoryQdslSql) this).onUpdate(clause, model, user);
            }
        }
    }

    private SQLQuery<?> prepareSelectQuery(C criteria, U user) {
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

    private OrderSpecifier<?> toOrderSpecifier(Sort.Order order) {
        ComparableExpressionBase<?> column = (ComparableExpressionBase<?>) this.getPath(order.getProperty());
        return order.isAscending() ? column.asc() : column.desc();
    }

    private Path<?> getPath(String columnName) {
        List<Path<?>> columns = this.getEntity().getColumns();
        for (Path<?> column : columns) {
            if (column.getMetadata().getName().equals(columnName)) {
                return column;
            }
        }
        throw new IllegalStateException(String.format("column[%s] not found in entity[%s] ", columnName, this.getEntity().getTableName()));
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
        query.orderBy(sort.stream().map(this::toOrderSpecifier).toArray(OrderSpecifier[]::new));
    }

    protected SQLQueryFactory getQueryFactory() {
        return queryFactory;
    }

    protected List<OrderSpecifier<?>> getDefaultOrders() {
        return List.of();
    }


}
