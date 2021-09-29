package org.bardframework.crud.api.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.apache.commons.lang3.SerializationUtils;
import org.bardframework.commons.utils.AssertionUtils;
import org.bardframework.commons.utils.CollectionUtils;
import org.bardframework.crud.api.event.ModelEventProducer;
import org.bardframework.crud.api.filter.IdFilter;
import org.bardframework.crud.api.util.PageableExecutionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

/**
 * Created by vahid on 1/17/17.
 */
public abstract class BaseServiceAbstract<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, D, R extends BaseRepository<M, C, I, U>, I extends Comparable<? super I>, U> implements BaseService<M, C, D, I, U> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    protected R repository;

    @Autowired
    ModelEventProducer eventProducer;

    @Autowired
    ObjectMapper mapper;

    public List<M> get(List<I> ids, U user) {
        List<M> list = this.getRepository().get(ids, user);
        return this.postFetch(list, user);
    }

    /**
     * get all data match with given <code>criteria</code>
     */
    public List<M> get(C criteria, U user) {
        List<M> list = this.getRepository().get(criteria, user);
        return this.postFetch(list, user);

    }

    /**
     * @return one entity with given criteria
     */
    public Optional<M> getOne(C criteria, U user) {
        return this.getRepository().getOne(criteria, user);
    }

    @Transactional
    public long delete(C criteria, U user) {
        List<M> models = this.getRepository().get(criteria, user);
        if (CollectionUtils.isEmpty(models)) {
            return 0;
        }
        for (M model : models) {
            this.preDelete(model, user);
        }
        /*
        call directDelete(List) instead of delete(List).
        maybe some joined part has been deleted in preDelete (like status change)
         */
        long deletedCount = this.getRepository().directDelete(models.stream().map(M::getId).collect(Collectors.toList()), user);

        getEventProducer().onDelete(models, user);

        if (deletedCount > 0) {
            for (M model : models) {
                this.postDelete(model, user);
            }
        }
        if (models.size() != deletedCount) {
            LOGGER.warn("deleting with criteria, expect delete {} item(s), but {} deleted.", models.size(), deletedCount);
        }
        return deletedCount;
    }

    @Transactional
    public long delete(List<I> ids, U user) {
        C criteria = this.getRepository().getEmptyCriteria();
        criteria.setId((IdFilter<I>) new IdFilter<I>().setIn(ids));
        return this.delete(criteria, user);
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
        C criteria = this.getRepository().getEmptyCriteria();
        criteria.setId((IdFilter<I>) new IdFilter<I>().setEquals(id));

        return this.delete(criteria, user);
    }

    /**
     * execute before deleting data
     */
    protected void preDelete(M model, U user) {
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
    public Optional<M> save(D dto, U user) {
        AssertionUtils.notNull(dto, "dto cannot be null.");
        this.preSave(dto, user);
        M model = this.getRepository().save(this.onSave(dto, user), user);
        getEventProducer().onSave(Collections.singletonList(model), user);
        this.postSave(model, dto, user);
        return this.get(model.getId(), user);
    }

    /**
     * save new data
     *
     * @return saved data models
     */
    @Transactional
    public List<M> save(List<D> dtos, U user) {
        AssertionUtils.notEmpty(dtos, "dtos cannot be null or empty.");
        List<M> list = new ArrayList<>();
        for (D dto : dtos) {
            this.preSave(dto, user);
            list.add(this.onSave(dto, user));
        }
        list = this.getRepository().save(list, user);
        getEventProducer().onSave(list, user);
        if (list.size() != dtos.size()) {
            throw new IllegalStateException("invalid save operation, save " + dtos.size() + " dtos, but result size is " + list.size());
        }
        for (int i = 0; i < list.size(); i++) {
            this.postSave(list.get(i), dtos.get(i), user);
        }
        return list;
    }

    /**
     * converting dto to model for save
     */
    protected abstract M onSave(D dto, U user);

    protected void preSave(D dto, U user) {
    }

    protected void postSave(M savedModel, D dto, U user) {
    }

    public Optional<M> patch(I id, Map<String, Object> fields, U user) {
        return repository.patch(id, fields, user);
    }

    @Transactional
    @Override
    public Optional<M> update(I id, D dto, U user) {
        Optional<M> model = this.getRepository().get(id, user);
        if (!model.isPresent()) {
            return Optional.empty();
        }

        this.preUpdate(model.get(), dto, user);
        M pre = SerializationUtils.clone(model.get());
        this.getRepository().update(this.onUpdate(dto, model.get(), user), user);
        getEventProducer().onUpdate(pre, model.get(), user);
        this.postUpdate(model.get(), dto, user);
        return this.get(model.get().getId(), user);
    }

    @Transactional
    @Override
    public Optional<M> patch(I id, JsonPatch patch, U user) throws JsonPatchException, JsonProcessingException {
        Optional<M> model = this.getRepository().get(id, user);
        if (!model.isPresent()) {
            return Optional.empty();
        }

//        this.preUpdate(model, dto, user);
        M pre = SerializationUtils.clone(model.get());
        M patched = applyPatchToCustomer(patch, model.get());
        this.getRepository().update(patched, user);
        getEventProducer().onUpdate(pre, model.get(), user);
//        this.postUpdate(model, dto, user);
        return this.getRepository().get(model.get().getId(), user);
    }

    private M applyPatchToCustomer(JsonPatch patch, M model) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(mapper.convertValue(model, JsonNode.class));
        return (M) mapper.treeToValue(patched, model.getClass());
    }

    protected abstract M onUpdate(D dto, M previousModel, U user);

    protected void preUpdate(M previousModel, D dto, U user) {
    }

    protected void postUpdate(M updatedModel, D dto, U user) {
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

    public ModelEventProducer getEventProducer() {
        return eventProducer;
    }

    public Logger getLogger() {
        return LOGGER;
    }


    @Override
    public final Page<M> get(C criteria, Pageable pageable, U user) {
        Page<M> page = this.getRepository().get(criteria, pageable, user);

        List<M> list = this.postFetch(page.toList(), user);
        return this.paging(list, pageable, page::getTotalElements);
    }

    protected List<M> postFetch(List<M> page, U user) {
        return page;
    }

    protected M postFetch(M model, U user) {
        return model;
    }

    /**
     * get by id
     */
    @Override
    public final Optional<M> get(I id, U user) {
        Optional<M> model = this.getRepository().get(id, user);
        return model.map(m -> postFetch(m, user));
    }

    protected Page<M> paging(List<M> list, Pageable pageable, LongSupplier supplier) {
        return pageable.isUnpaged() ? new PageImpl<>(list) : PageableExecutionUtils.getPage(list, pageable, supplier);
    }

    public List<M> getAll(U user) {
        List<M> list = this.getRepository().getAll(user);
        return this.postFetch(list, user);
    }
}
