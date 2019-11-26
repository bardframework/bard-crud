package org.bardframework.base.crud;

import com.querydsl.core.dml.StoreClause;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import org.bardframework.commons.util.AssertionUtils;
import org.bardframework.commons.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

/**
 * Created by vahid on 1/17/17.
 */
public abstract class BaseRepositoryQdslSqlAbstract<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, I extends Serializable, U> implements BaseRepository<M, C, I, U> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected final Class<M> modelClazz;
    protected final Class<C> criteriaClazz;

    @Autowired
    private SQLQueryFactory queryFactory;

    public BaseRepositoryQdslSqlAbstract() {
        ParameterizedType parameterizedType = null;
        Class<?> targetClazz = this.getClass();
        while (!(null != parameterizedType && parameterizedType.getActualTypeArguments().length >= 2) && null != targetClazz) {
            parameterizedType = targetClazz.getGenericSuperclass() instanceof ParameterizedType ? (ParameterizedType) targetClazz.getGenericSuperclass() : null;
            targetClazz = targetClazz.getSuperclass();
        }
        try {
            this.modelClazz = (Class<M>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            this.criteriaClazz = (Class<C>) parameterizedType.getActualTypeArguments()[1];
        } catch (Exception e) {
            this.LOGGER.debug("can't determine class from generic type!", e);
            throw new IllegalArgumentException("can't determine class from generic type!", e);
        }
    }

    protected abstract <T> SQLQuery<T> setCriteria(C criteria, SQLQuery<T> query, U user);

    protected abstract RelationalPathBase<?> getEntity();

    protected abstract QBean<M> getQBean();

    protected abstract <T extends StoreClause<T>> T toClause(T clause, M model, U user);

    protected abstract M setIdentifier(M model, U user);

    public abstract <T extends SimpleExpression<I>> T getIdentifierPath();

    public M getEmptyModel() {
        try {
            return modelClazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            this.LOGGER.error("can't instantiate model class using empty constructor {}", this.modelClazz, e);
            throw new IllegalArgumentException("can't instantiate model class using empty constructor" + this.modelClazz, e);
        }
    }

    public C getEmptyCriteria() {
        C criteria;
        try {
            criteria = criteriaClazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            this.LOGGER.error("can't instantiate criteria class using empty constructor {}", this.criteriaClazz, e);
            throw new IllegalArgumentException("can't instantiate criteria class using empty constructor" + this.criteriaClazz, e);
        }
        return criteria;
    }

    protected <T extends StoreClause<T>> T fillClause(T clause, M model, U user) {
        clause = this.toClause(clause, model, user);
        for (Class clazz : this.getClass().getInterfaces()) {
            if (WriteExtendedRepositoryQdslSql.class.isAssignableFrom(clazz)) {
                ((WriteExtendedRepositoryQdslSql) this).process(clause, model, user);
            }
        }
        return clause;
    }

    @Transactional
    @Override
    public M save(M model, U user) {
        return this.save(Collections.singletonList(model), user).get(0);
    }

    /**
     * @param models
     * @param user
     * @return not changed models if models is null or empty, saved models otherwise.
     */
    @Transactional
    @Override
    public List<M> save(List<M> models, U user) {
        if (CollectionUtils.isEmpty(models)) {
            return models;
        }
        SQLInsertClause insertClause = this.getQueryFactory().insert(this.getEntity());
        models.forEach(model -> {
                    this.setIdentifier(model, user);
                    AssertionUtils.notNull(model.getId(), "model identifier is not provided in 'setIdentifier' method");
            insertClause.set(getIdentifierPath(), model.getId());
            this.fillClause(insertClause, model, user);
                    insertClause.addBatch();
                }
        );
        long affectedRowsCount = insertClause.execute();
        if (models.size() != affectedRowsCount) {
            LOGGER.warn("expect insert '{}' row, but '{}' row(s) inserted.", models.size(), affectedRowsCount);
//            throw new IllegalStateException("expect affect '" + models.size() + "' row, but " + affectedRowsCount + " row(s) inserted.");
        }
        return models;
    }

    @Transactional
    @Override
    public M update(M model, U user) {
        SQLUpdateClause updateClause = this.getQueryFactory().update(getEntity()).where(this.getIdentifierPath().eq(model.getId()));
        updateClause = this.fillClause(updateClause, model, user);
        long affectedRowsCount = updateClause.execute();
        if (1 != affectedRowsCount) {
            throw new IllegalStateException("expect affect one row, but " + affectedRowsCount + " row(s) updated.");
        }
        return model;
    }

    @Transactional(readOnly = true)
    @Override
    public M get(I identifier, U user) {
        AssertionUtils.notNull(identifier, "Given Identifier cannot be null.");
        C criteria = this.getEmptyCriteria();
        criteria.setIds(Collections.singletonList(identifier));
        return this.getOne(criteria, user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<M> get(List<I> ids, U user) {
        AssertionUtils.notNull(ids, "Given Identifiers cannot be null.");
        C criteria = this.getEmptyCriteria();
        criteria.setIds(ids);
        return this.get(criteria, user);
    }

    public <T> SQLQuery<T> setPageAndSize(C criteria, SQLQuery<T> query, U user) {
        if (criteria.getSize() < 1 || criteria.getPage() < 1) {
            throw new IllegalArgumentException("page and size must be greater than 1");
        }
        query.limit(criteria.getSize());
        query.offset((criteria.getPage() - 1) * criteria.getSize());
        return query;
    }

    public SQLQuery<?> prepareQuery(C criteria, U user) {
        SQLQuery<?> query = this.getQueryFactory().query();
        query.from(this.getEntity());
        query = this.setJoins(query, user);
        query = this.setCriteria(criteria, query, user);
        if (null != criteria.getExcludes()) {
            query.where(this.getIdentifierPath().notIn(criteria.getExcludes()));
        }
        if (null != criteria.getIds()) {
            query.where(this.getIdentifierPath().in(criteria.getIds()));
        }
        for (Class clazz : this.getClass().getInterfaces()) {
            if (ReadExtendedRepositoryQdslSql.class.isAssignableFrom(clazz)) {
                ((ReadExtendedRepositoryQdslSql) this).process(criteria, query, user);
            }
        }
        this.setOrders(query, criteria, user);
        return query;
    }

    @Transactional(readOnly = true)
    @Override
    public DataTableModel<M> filter(C criteria, U user) {
        AssertionUtils.notNull(criteria, "null criteria not acceptable");
        SQLQuery<?> query = this.prepareQuery(criteria, user);
        long count = query.fetchCount();
        if (0 > count) {
            return new DataTableModel<>();
        }
        query = this.prepareQuery(criteria, user);
        query = this.setPageAndSize(criteria, query, user);
        return new DataTableModel<>(query.select(this.getQBean()).fetch(), count);
    }

    @Transactional(readOnly = true)
    @Override
    public long getCount(C criteria, U user) {
        return this.prepareQuery(criteria, user).fetchCount();
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
        AssertionUtils.notNull(ids, "ids should not be null.");
        C criteria = this.getEmptyCriteria();
        criteria.setIds(ids);
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
        return this.prepareQuery(criteria, user).select(this.getIdentifierPath()).fetch();
    }

    @Transactional(readOnly = true)
    @Override
    public List<M> get(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null");
        return this.prepareQuery(criteria, user).select(this.getQBean()).fetch();
    }

    @Transactional(readOnly = true)
    @Override
    public M getOne(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null");
        return this.prepareQuery(criteria, user).select(this.getQBean()).fetchOne();
    }

    protected <T> SQLQuery<T> setJoins(SQLQuery<T> query, U user) {
        return query;
    }

    public <R> SQLQuery<R> setOrders(SQLQuery<R> query, C criteria, U user) {
        return query;
    }

    public SQLQueryFactory getQueryFactory() {
        return queryFactory;
    }

    protected <T extends Serializable> T safeFetchId(BaseModelAbstract<T> model) {
        return null == model ? null : model.getId();
    }

}
