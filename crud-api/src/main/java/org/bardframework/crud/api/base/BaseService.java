package org.bardframework.crud.api.base;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.bardframework.commons.utils.AssertionUtils;
import org.bardframework.commons.utils.ReflectionUtils;
import org.bardframework.form.model.filter.IdFilter;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vahid on 1/17/17.
 */
public abstract class BaseService<M extends BaseModel<I>, C extends BaseCriteria<I>, D, R extends BaseRepository<M, C, I, U>, I extends Serializable, U> extends ReadService<M, C, R, I, U> {

    protected final Class<D> dtoClazz;

    public BaseService(R repository) {
        super(repository);
        this.dtoClazz = ReflectionUtils.getGenericArgType(this.getClass(), 2);
    }

    /**
     * delete data with given id
     *
     * @param id identifier of data that must be delete
     * @return count of deleted data
     */
    @Transactional
    public long delete(I id, U user) {
        AssertionUtils.notNull(id, "id cannot be null.");
        return this.delete(List.of(id), user);
    }

    @Transactional
    public long delete(Collection<I> ids, U user) {
        AssertionUtils.notEmpty(ids, "Given ids cannot be empty.");
        if (ids.isEmpty()) {
            return 0;
        }
        C criteria = this.getEmptyCriteria();
        criteria.setIdFilter(new IdFilter<I>().setIn(ids));
        return this.delete(criteria, user);
    }

    @Transactional
    public long delete(C criteria, U user) {
        AssertionUtils.notNull(criteria, "Given criteria cannot be null.");
        List<M> models = this.getRepository().get(criteria, user);
        if (CollectionUtils.isEmpty(models)) {
            return 0;
        }
        this.preDelete(criteria, models, user);
        /*
            call directDelete(List) instead of delete(List).
            maybe some joined part has been deleted in preDelete (like status change)
         */
        C deleteCriteria = this.getEmptyCriteria();
        deleteCriteria.setIdFilter(new IdFilter<I>().setIn(models.stream().map(M::getId).collect(Collectors.toList())));
        long deletedCount = this.getRepository().delete(deleteCriteria, user);

        this.postDelete(criteria, models, deletedCount, user);
        return deletedCount;
    }

    /**
     * execute before deleting data
     */
    protected void preDelete(C criteria, List<M> models, U user) {
        for (M model : models) {
            this.preDelete(criteria, model, user);
        }
    }

    protected void preDelete(C criteria, M model, U user) {
    }

    protected void postDelete(C criteria, List<M> deletedModels, long deletedCount, U user) {
        if (deletedModels.size() != deletedCount) {
            log.warn("deleting with criteria, expect delete {} item(s), but {} deleted.", deletedModels.size(), deletedCount);
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
        AssertionUtils.notNull(dtos, "Given dtos cannot be null.");
        if (dtos.isEmpty()) {
            return Collections.emptyList();
        }
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
            log.warn("saving dtos, expect save {} item(s), but {} saved.", dtos.size(), savedModels.size());
        }
        for (int i = 0; i < savedModels.size(); i++) {
            this.postSave(savedModels.get(i), dtos.get(i), user);
        }
    }

    protected void postSave(M savedModel, D dto, U user) {
    }

    @Transactional
    public M patch(I id, Map<String, Object> patch, U user) {
        AssertionUtils.notNull(id, "id cannot be null.");
        AssertionUtils.notEmpty(patch, "patch cannot be empty.");
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
    public M update(I id, D dto, U user) {
        AssertionUtils.notNull(id, "id cannot be null.");
        AssertionUtils.notNull(dto, "patch cannot be dto.");
        M entity = this.getRepository().get(id, user);
        if (null == entity) {
            return null;
        }
        M pre = SerializationUtils.clone(entity);
        this.preUpdate(pre, dto, user);
        this.onUpdate(dto, entity, user);
        M updated = this.getRepository().update(entity, user);
        this.postUpdate(pre, updated, dto, user);
        return this.get(entity.getId(), user);
    }

    protected abstract void onUpdate(D dto, M previousModel, U user);

    protected void preUpdate(M previousModel, D dto, U user) {
    }

    protected void postUpdate(M previousModel, M updatedModel, D dto, U user) {
    }
}
