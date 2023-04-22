package org.bardframework.crud.api.base;

import org.apache.commons.collections4.CollectionUtils;
import org.bardframework.commons.utils.AssertionUtils;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.form.model.filter.IdFilter;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by vahid on 1/17/17.
 */
public abstract class ReadService<M extends BaseModel<I>, C extends BaseCriteria<I>, R extends ReadRepository<M, C, I, U>, I, U> {

    protected final Class<M> modelClazz;
    protected final Class<C> criteriaClazz;
    protected final R repository;

    public ReadService(R repository) {
        this.repository = repository;
        this.modelClazz = ReflectionUtils.getGenericArgType(this.getClass(), 0);
        this.criteriaClazz = ReflectionUtils.getGenericArgType(this.getClass(), 1);
    }

    public C getEmptyCriteria() {
        return ReflectionUtils.newInstance(criteriaClazz);
    }

    public List<M> get(Collection<I> ids, U user) {
        AssertionUtils.notEmpty(ids, "Given ids cannot be empty.");
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        C criteria = this.getEmptyCriteria();
        criteria.setIdFilter(new IdFilter<I>().setIn(ids));
        return this.get(criteria, user);
    }

    /**
     * get by id
     */
    public M get(I id, U user) {
        AssertionUtils.notNull(id, "Given id cannot be null.");
        C criteria = this.getEmptyCriteria();
        criteria.setIdFilter(new IdFilter<I>().setEquals(id));
        List<M> models = this.get(criteria, user);
        if (CollectionUtils.isEmpty(models)) {
            return null;
        }
        return models.get(0);
    }


    public List<M> get(U user) {
        return this.get(this.getEmptyCriteria(), user);
    }

    /**
     * get all data match with given <code>criteria</code>
     */
    public List<M> get(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        this.preFetch(criteria, user);
        List<M> list = this.getRepository().get(criteria, user);
        this.postFetch(criteria, list, user);
        return list;
    }

    /**
     * @return one entity with given criteria
     */
    public M getOne(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        this.preFetch(criteria, user);
        M model = this.getRepository().getOne(criteria, user);
        this.postFetch(criteria, Collections.singletonList(model), user);
        return model;
    }

    public PagedData<M> get(C criteria, Pageable pageable, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        AssertionUtils.notNull(pageable, "Given pageable cannot be null.");
        this.preFetch(criteria, user);
        PagedData<M> pagedData = this.getRepository().get(criteria, pageable, user);
        this.postFetch(criteria, pagedData.getData(), user);
        return pagedData;
    }

    protected void preFetch(C criteria, U user) {
    }

    protected void postFetch(C criteria, List<M> result, U user) {
        for (M model : result) {
            this.postFetch(model, user);
        }
    }

    protected void postFetch(M model, U user) {
    }

    public List<I> getIds(C criteria, U user) {
        return this.getRepository().getIds(criteria, user);
    }

    public long getCount(C criteria, U user) {
        return this.getRepository().getCount(criteria, user);
    }

    public boolean isExist(C criteria, U user) {
        return this.getRepository().isExist(criteria, user);
    }

    public boolean isNotExist(C criteria, U user) {
        return this.getRepository().isNotExist(criteria, user);
    }

    public R getRepository() {
        return repository;
    }
}
