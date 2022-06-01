package org.bardframework.crud.api.base;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.bardframework.commons.utils.AssertionUtils;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.crud.api.filter.IdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by vahid on 1/17/17.
 */
public abstract class BaseServiceAbstract<M extends BaseModel<I>, C extends BaseCriteriaAbstract<I>, D, R extends BaseRepository<M, C, I, U>, I extends Comparable<? super I>, U> implements BaseService<M, C, D, I, U> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected final Class<M> modelClazz;
    protected final Class<C> criteriaClazz;
    protected final Class<D> dtoClazz;
    protected final R repository;

    public BaseServiceAbstract(R repository) {
        this.repository = repository;
        this.modelClazz = ReflectionUtils.getGenericClass(this.getClass(), 0);
        this.criteriaClazz = ReflectionUtils.getGenericSuperClass(this.getClass(), 1);
        this.dtoClazz = ReflectionUtils.getGenericSuperClass(this.getClass(), 2);
    }

    protected C getEmptyCriteria() {
        return ReflectionUtils.newInstance(criteriaClazz);
    }

    public List<M> get(List<I> ids, U user) {
        C criteria = this.getEmptyCriteria();
        criteria.setId((IdFilter<I>) new IdFilter<I>().setIn(ids));
        return this.get(criteria, user);
    }

    /**
     * get by id
     */
    @Override
    public M get(I id, U user) {
        C criteria = this.getEmptyCriteria();
        criteria.setId((IdFilter<I>) new IdFilter<I>().setEquals(id));
        List<M> models = this.get(criteria, user);
        if (CollectionUtils.isEmpty(models)) {
            return null;
        }
        this.postFetch(criteria, models, user);
        return models.get(0);
    }


    public List<M> get(U user) {
        return this.get(this.getEmptyCriteria(), user);
    }

    /**
     * get all data match with given <code>criteria</code>
     */
    public List<M> get(C criteria, U user) {
        this.preFetch(criteria, user);
        List<M> list = this.getRepository().get(criteria, user);
        this.postFetch(criteria, list, user);
        return list;
    }

    /**
     * @return one entity with given criteria
     */
    public M getOne(C criteria, U user) {
        this.preFetch(criteria, user);
        M model = this.getRepository().getOne(criteria, user);
        this.postFetch(criteria, Collections.singletonList(model), user);
        return model;
    }

    @Override
    public final Page<M> get(C criteria, Pageable pageable, U user) {
        this.preFetch(criteria, user);
        Page<M> page = this.getRepository().get(criteria, pageable, user);
        this.postFetch(criteria, page.toList(), user);
        return page;
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

    /**
     * delete data with given id
     *
     * @param id identifier of data that must be delete
     * @return count of deleted data
     */
    @Transactional
    @Override
    public long delete(I id, U user) {
        AssertionUtils.notNull(id, "id cannot be null.");
        return this.delete(List.of(id), user);
    }

    @Transactional
    public long delete(List<I> ids, U user) {
        AssertionUtils.notEmpty(ids, "ids cannot be empty.");
        C criteria = this.getEmptyCriteria();
        criteria.setId((IdFilter<I>) new IdFilter<I>().setIn(ids));
        return this.delete(criteria, user);
    }

    @Transactional
    public long delete(C criteria, U user) {
        AssertionUtils.notNull(criteria, "criteria cannot be null.");
        List<M> models = this.getRepository().get(criteria, user);
        if (CollectionUtils.isEmpty(models)) {
            return 0;
        }
        this.preDelete(criteria, models, user);
        /*
            call directDelete(List) instead of delete(List).
            maybe some joined part has been deleted in preDelete (like status change)
         */
        long deletedCount = this.getRepository().directDelete(models.stream().map(M::getId).collect(Collectors.toList()), user);

        this.postDelete(criteria, models, deletedCount, user);
        return deletedCount;
    }

    /**
     * execute before deleting data
     */
    protected void preDelete(C criteria, List<M> models, U user) {
        for (M model : models) {
            this.preDelete(model, user);
        }
    }

    protected void preDelete(M model, U user) {
    }

    protected void postDelete(C criteria, List<M> deletedModels, long deletedCount, U user) {
        if (deletedModels.size() != deletedCount) {
            LOGGER.warn("deleting with criteria, expect delete {} item(s), but {} deleted.", deletedModels.size(), deletedCount);
        }
        for (M model : deletedModels) {
            this.postDelete(model, user);
        }
    }

    /**
     * execute after deleting data
     */
    protected void postDelete(M deletedModel, U user) {
    }

    /**
     * save new data
     *
     * @return saved data model
     */
    @Transactional
    @Override
    public M save(D dto, U user) {
        AssertionUtils.notNull(dto, "dto cannot be null.");
        return this.save(List.of(dto), user).get(0);
    }

    /**
     * save new data
     *
     * @return saved data models
     */
    @Transactional
    public List<M> save(List<D> dtos, U user) {
        AssertionUtils.notEmpty(dtos, "dtos cannot be empty.");
        this.preSave(dtos, user);
        List<M> list = new ArrayList<>();
        for (D dto : dtos) {
            list.add(this.onSave(dto, user));
        }
        list = this.getRepository().save(list, user);
        this.postSave(dtos, list, user);
        return list.stream().map(model -> this.get(model.getId(), user)).collect(Collectors.toList());
    }

    /**
     * converting dto to model for save
     */
    protected abstract M onSave(D dto, U user);

    protected void preSave(List<D> dtos, U user) {
        for (D dto : dtos) {
            this.preSave(dto, user);
        }
    }

    protected void preSave(D dto, U user) {
    }

    protected void postSave(List<D> dtos, List<M> savedModels, U user) {
        if (savedModels.size() != dtos.size()) {
            LOGGER.warn("saving dtos, expect save {} item(s), but {} saved.", dtos.size(), savedModels.size());
        }
        for (int i = 0; i < savedModels.size(); i++) {
            this.postSave(savedModels.get(i), dtos.get(i), user);
        }
    }

    protected void postSave(M savedModel, D dto, U user) {
    }

    @Transactional
    @Override
    public M patch(I id, Map<String, Object> patch, U user) {
        M model = this.getRepository().get(id, user);
        if (null == model) {
            return null;
        }
        M pre = SerializationUtils.clone(model);
        this.prePatch(pre, patch, user);
        M patched = this.getRepository().patch(id, patch, user);
        this.postPatch(pre, patched, patch, user);
        return this.get(model.getId(), user);
    }

    protected void prePatch(M previousModel, Map<String, Object> patch, U user) {
    }

    protected void postPatch(M previousModel, M patchedModel, Map<String, Object> patch, U user) {
    }

    @Transactional
    @Override
    public M update(I id, D dto, U user) {
        M model = this.getRepository().get(id, user);
        if (null == model) {
            return null;
        }
        M pre = SerializationUtils.clone(model);
        this.preUpdate(pre, dto, user);
        M updated = this.getRepository().update(this.onUpdate(dto, model, user), user);
        this.postUpdate(pre, updated, dto, user);
        return this.get(model.getId(), user);
    }

    protected abstract M onUpdate(D dto, M previousModel, U user);

    protected void preUpdate(M previousModel, D dto, U user) {
    }

    protected void postUpdate(M previousModel, M updatedModel, D dto, U user) {
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

    protected R getRepository() {
        return repository;
    }
}
