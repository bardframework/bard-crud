package org.bardframework.crud.api.base;

import org.apache.commons.lang3.RandomUtils;
import org.bardframework.form.model.filter.IdFilter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created on 08/05/2017.
 */
public interface RepositoryDataProvider<M extends BaseModel<I>, C extends BaseCriteria<I>, R extends BaseRepository<M, C, I, U>, I, U> {

    U getUser();

    I getInvalidId();

    M getUnsavedModel();

    void assertEqualUpdate(M first, M second);

    void makeInvalid(M model);

    C getEmptyCriteria();

    R getRepository();

    boolean isDuplicate(M first, M second, U user);

    default I getId(U user) {
        return this.getId(this.getEmptyCriteria(), this.getUnsavedModel(), user);
    }

    //Model...
    default List<M> getUnsavedModels(long count, U user) {
        List<M> models = new ArrayList<>();
        while (models.size() < count) {
            M model = this.getUnsavedModel();
            if (models.stream().noneMatch(element -> this.isDuplicate(element, model, user))) {
                models.add(model);
            }
        }
        return models;
    }


    default M getModel(U user) {
        M unsavedModel = this.getUnsavedModel();
        return this.getModel(this.getEmptyCriteria(), unsavedModel, user);
    }

    default M getInvalidModel(U user) {
        M model = this.getModel(user);
        this.makeInvalid(model);
        return model;
    }

    default M getUnsavedInvalidModel() {
        M unsavedModel = this.getUnsavedModel();
        this.makeInvalid(unsavedModel);
        return unsavedModel;
    }
    //...Model

    //Filter...
    default C getFilterCriteria() {
        return this.getEmptyCriteria();
    }

    default Pageable getPageable() {
        return PageRequest.of(1, 5);
    }

    //...Filter

    default I getId(C criteria, M unsavedModel, U user) {
        return this.getModel(criteria, unsavedModel, user).getId();
    }

    default List<I> getIds(int count, U user, I... excludeIds) {
        return this.getModels(count, user, excludeIds).stream().map(M::getId).collect(Collectors.toList());
    }

    default List<M> getModels(int count, U user, I... excludeIds) {
        C criteria = this.getEmptyCriteria();
        if (excludeIds.length > 0) {
            criteria.setIdFilter(new IdFilter<I>().setNotIn(List.of(excludeIds)));
        }
        this.saveNew(count - this.getRepository().getCount(criteria, user), user);
        return this.getRepository().get(criteria, PageRequest.of(1, count), user).getData();
    }

    default M getModel(C criteria, M unsavedModel, U user) {
        long count = this.getRepository().getCount(criteria, user);
        if (0 == count) {
            //TODO save model if pass given criteria restrictions
            return this.getRepository().save(unsavedModel, user);
        }
        return this.getRepository().get(criteria, PageRequest.of(RandomUtils.nextInt(1, (int) count), 1), user).getData().get(0);
    }

    /**
     * save <count>count<count/> new entities
     *
     * @return saved entities
     */
    @Transactional
    default List<M> saveNew(long count, U user) {
        List<M> unsavedModels = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            unsavedModels.add(this.getRepository().save(this.getUnsavedModel(), user));
        }
        return unsavedModels;
    }

    /**
     * @return a model that pass <code>validateFunction</code>, saved <code>unsavedModel</code> otherwise.
     */
    default M getOrSave(M unsavedModel, U user, Function<M, Boolean> validateFunction) {
        M model = this.getModel(validateFunction, user);
        if (null == model) {
            return this.getRepository().save(unsavedModel, user);
        }
        return model;
    }

    /**
     * @return a model that pass <code>validateFunction</code>, null otherwise
     */
    default M getModel(Function<M, Boolean> validateFunction, U user) {
        C criteria = this.getEmptyCriteria();
        long count = this.getRepository().getCount(criteria, user);
        if (count == 0) {
            return null;
        }
        for (int i = 0; i < count; i++) {
            M model = this.getRepository().get(criteria, PageRequest.of(i, 1), user).getData().get(0);
            if (validateFunction.apply(model)) {
                return model;
            }
        }
        return null;
    }

    default boolean isExist(C criteria, U user) {
        return this.getRepository().isExist(criteria, user);
    }

    default void assertEqualSave(M first, M second) {
        this.assertEqualUpdate(first, second);
    }
}
