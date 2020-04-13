package org.bardframework.base.crud;

import org.assertj.core.api.Assertions;
import org.bardframework.commons.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Sama-PC on 08/05/2017.
 */
public abstract class DataProviderRepositoryAbstract<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, R extends BaseRepository<M, C, I, U>, I extends Number & Comparable<I>, U> {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected R repository;

    protected Class<C> criteriaClazz;

    public DataProviderRepositoryAbstract() {
        Class<?> targetCLazz = this.getClass();
        while (null != targetCLazz && !(targetCLazz.getGenericSuperclass() instanceof ParameterizedType)) {
            targetCLazz = targetCLazz.getSuperclass();
        }
        try {
            ParameterizedType parameterizedType = (ParameterizedType) targetCLazz.getGenericSuperclass();
            this.criteriaClazz = (Class) parameterizedType.getActualTypeArguments()[1];
        } catch (Exception e) {
            this.LOGGER.debug("can't determine class from generic type!", e);
            throw new IllegalArgumentException("can't determine class from generic type!", e);
        }
    }

    public C getEmptyCriteria() {
        C criteria;
        try {
            criteria = criteriaClazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(e);
        }
        return criteria;
    }

    public I getId(U user) {
        return this.getId(this.getEmptyCriteria(), this.getUnsavedModel(user), user);
    }

    //Model...
    public List<M> getUnsavedModels(long count, U user) {
        List<M> models = new ArrayList<>();
        while (models.size() < count) {
            M model = this.getUnsavedModel(user);
            if (models.stream().noneMatch(element -> this.isDuplicate(element, model, user))) {
                models.add(model);
            }
        }
        return models;
    }

    protected boolean isDuplicate(M first, M second, U user) {
        return false;
    }

    public M getModel(U user) {
        return this.getModel(this.getEmptyCriteria(), this.getUnsavedModel(user), user);
    }

    public M getInvalidModel(U user) {
        return this.makeInvalid(this.getModel(user));
    }

    public M getUnsavedInvalidModel(U user) {
        return this.makeInvalid(this.getUnsavedModel(user));
    }
    //...Model

    //Filter...
    public C getCriteria() {
        C criteria = getEmptyCriteria();
        criteria.setPage(1);
        criteria.setSize(5);
        return criteria;
    }

    /**
     * It is highly recommended to implement this method.
     */
    public C getInvalidCriteria() {
        C criteria = getEmptyCriteria();
        criteria.setPage(-1);
        criteria.setSize(-1);
        return criteria;
    }
    //...Filter


    protected <I extends Number & Comparable<I>> void assertNullOrEqualIds(BaseModelAbstract<I> first, BaseModelAbstract<I> second) {
        Assertions.assertThat(first == null ^ second == null).isFalse();
        if (first != null) {
            Assertions.assertThat(first.getId()).isEqualByComparingTo(second.getId());
        }
    }

    public I getId(C criteria, M unsavedModel, U user) {
        return this.getModel(criteria, unsavedModel, user).getId();
    }

    public List<I> getIds(int count, U user, I... excludeIds) {
        return this.getModels(count, user, excludeIds).stream().map(M::getId).collect(Collectors.toList());
    }

    public List<M> getModels(int count, U user, I... excludeIds) {
        C criteria = this.getEmptyCriteria();
        if (excludeIds.length > 0) {
            criteria.setExcludes(Arrays.asList(excludeIds));
        }
        this.saveNew(count - repository.getCount(criteria, user), user);
        criteria.setPage(1);
        criteria.setSize(count);
        return this.repository.filter(criteria, user).getList();
    }

    public M getModel(C criteria, M unsavedModel, U user) {
        long count = repository.getCount(criteria, user);
        if (0 == count) {
            //TODO save model that pass give criteria restrictions
            return repository.save(unsavedModel, user);
        }
        criteria.setPage(RandomUtils.nextLong(1, count));
        criteria.setSize(1);
        return repository.filter(criteria, user).getList().get(0);
    }

    /**
     * save <count>count<count/> new entities
     *
     * @param count
     * @return saved entities
     */
    @Transactional
    public List<M> saveNew(long count, U user) {
        List<M> unsavedModels = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            unsavedModels.add(repository.save(this.getUnsavedModel(user), user));
        }
        return unsavedModels;
    }

    /**
     * @param unsavedModel
     * @param user
     * @param validateFunction
     * @return a model that pass <code>validateFunction</code>, saved <code>unsavedModel</code> otherwise.
     */
    public M getOrSave(M unsavedModel, U user, Function<M, Boolean> validateFunction) {
        M model = this.getModel(validateFunction, user);
        return null == model ? repository.save(unsavedModel, user) : model;
    }

    /**
     * @param validateFunction
     * @return a model that pass <code>validateFunction</code>, null otherwise
     */
    public M getModel(Function<M, Boolean> validateFunction, U user) {
        C criteria = this.getEmptyCriteria();
        long count = repository.getCount(criteria, user);
        if (count == 0) {
            return null;
        }
        criteria.setSize(1);
        for (int i = 0; i < count; i++) {
            criteria.setPage(i + 1);
            M model = repository.filter(criteria, user).getList().get(0);
            if (validateFunction.apply(model)) {
                return model;
            }
        }
        return null;
    }

    public boolean isExist(C criteria, U user) {
        return repository.isExist(criteria, user);
    }

    public abstract I getInvalidId();

    public abstract M getUnsavedModel(U user);

    public void assertEqualSave(M first, M second) {
        this.assertEqualUpdate(first, second);
    }

    public abstract void assertEqualUpdate(M first, M second);

    protected abstract M makeInvalid(M model);
}