package org.bardframework.crud.impl.querydsl.base;

import com.querydsl.core.dml.StoreClause;
import com.querydsl.core.types.Path;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.bardframework.commons.utils.AssertionUtils;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.base.BaseRepository;
import org.bardframework.form.model.filter.IdFilter;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by vahid on 1/17/17.
 */
@Slf4j
public abstract class BaseRepositoryQdslSqlAbstract<M extends BaseModel<I>, C extends BaseCriteria<I>, I, U> extends ReadRepositoryQdslSqlAbstract<M, C, I, U> implements BaseRepository<M, C, I, U> {

    public BaseRepositoryQdslSqlAbstract(SQLQueryFactory queryFactory) {
        super(queryFactory);
    }

    protected abstract <T extends StoreClause<T>> void onSave(T clause, M model, U user);

    protected abstract <T extends StoreClause<T>> void onUpdate(T clause, M model, U user);

    protected abstract I generateId(M entity, U user);

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
        this.setIds(list, user);
        list.forEach(model -> {
                    this.onSaveInternal(insertClause, model, user);
                    insertClause.addBatch();
                }
        );
        Long affectedCount = this.insertAndSetIds(list, insertClause);
        if (null != affectedCount && list.size() != affectedCount) {
            log.debug("expect insert '{}' row, but '{}' row(s) inserted.", list.size(), affectedCount);
        }
        return list;
    }

    protected void setIds(List<M> list, U user) {
        list.forEach(model -> model.setId(this.generateId(model, user)));
    }

    /**
     * @return count of inserted records
     */
    protected Long insertAndSetIds(List<M> list, SQLInsertClause insertClause) {
        if (!(this.getIdSelectExpression() instanceof Path)) {
            return insertClause.execute();
        }
        List<I> generatedIds = this.insert(insertClause);
        if (CollectionUtils.isNotEmpty(generatedIds)) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setId(generatedIds.get(i));
            }
        }
        /*
            در حالتی که شناسه‌ها در دیتابیس تولید نمی‌شوند؛ این لیست خالی است.
         */
        return CollectionUtils.isNotEmpty(generatedIds) ? (long) generatedIds.size() : null;
    }

    /**
     * @return list of ids of inserted records
     */
    protected List<I> insert(SQLInsertClause insertClause) {
        return insertClause.executeWithKeys((Path<I>) this.getIdSelectExpression());
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
        for (M model : models) {
            AssertionUtils.notNull(model.getId(), "identifier is not provided, can't update");
            updateClause.where(this.getPredicate(new IdFilter<I>().setEquals(model.getId()), user));
            this.onUpdateInternal(updateClause, model, user);
            updateClause.addBatch();
        }
        long affectedRowsCount = updateClause.execute();
        if (models.size() != affectedRowsCount) {
            log.debug("expect update '{}' row, but '{}' row(s) updated.", models.size(), affectedRowsCount);
        }
        return new ArrayList<>(models);
    }

    protected void update(I identifier, Consumer<SQLUpdateClause> onUpdate, U user) {
        AssertionUtils.notNull(identifier, "Given identifier cannot be null.");
        AssertionUtils.notNull(onUpdate, "onUpdate cannot be null.");
        C criteria = ReflectionUtils.newInstance(criteriaClazz);
        criteria.setIdFilter(new IdFilter<I>().setEquals(identifier));
        long affectedRowsCount = this.update(criteria, onUpdate, user);
        if (1 != affectedRowsCount) {
            log.error("expect update '{}' row, but '1' row(s) updated.", affectedRowsCount);
            throw new IllegalStateException("affected rows in update not valid");
        }
    }

    protected long update(C criteria, Consumer<SQLUpdateClause> onUpdate, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        AssertionUtils.notNull(onUpdate, "onUpdate cannot be null.");
        SQLUpdateClause updateClause = this.getQueryFactory().update(this.getEntity());
        updateClause.where(this.getPredicate(criteria, user));
        onUpdate.accept(updateClause);
        return updateClause.execute();
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

    protected <T extends StoreClause<T>> void onSaveInternal(T clause, M model, U user) {
        this.onSave(clause, model, user);
        for (Class<?> clazz : this.getClass().getInterfaces()) {
            if (SaveExtendedRepositoryQdslSql.class.isAssignableFrom(clazz)) {
                ((SaveExtendedRepositoryQdslSql) this).onSave(clause, model, user);
            }
        }
    }

    protected <T extends StoreClause<T>> void onUpdateInternal(T clause, M model, U user) {
        this.onUpdate(clause, model, user);
        for (Class<?> clazz : this.getClass().getInterfaces()) {
            if (UpdateExtendedRepositoryQdslSql.class.isAssignableFrom(clazz)) {
                ((UpdateExtendedRepositoryQdslSql) this).onUpdate(clause, model, user);
            }
        }
    }
}
