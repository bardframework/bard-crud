package org.bardframework.crud.api.base;

import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created on 08/05/2017.
 */
public interface DataProviderService<M extends BaseModel<I>, C extends BaseCriteria<I>, D, S extends BaseService<M, C, D, R, I, U>, R extends BaseRepository<M, C, I, U>, I extends Serializable, U> extends DataProviderRepository<M, C, R, I, U> {

    S getService();

    void makeInvalid(D dto);

    D getDto();

    void assertEqualUpdate(M model, D dto);

    @Override
    default R getRepository() {
        return this.getService().getRepository();
    }

    @Override
    default M getUnsavedModel() {
        return this.toModel(this.getDto());
    }

    default List<D> getUnsavedDtos(long count, U user) {
        List<D> models = new ArrayList<>();
        while (models.size() < count) {
            D dto = this.getDto();
            if (models.stream().noneMatch(element -> this.isDuplicate(element, dto, user))) {
                models.add(dto);
            }
        }
        return models;
    }

    /**
     * use {@link #isDuplicate(M, M, U)} in default implementation
     */
    default boolean isDuplicate(D first, D second, U user) {
        return this.isDuplicate(this.toModel(first), this.toModel(second), user);
    }

    //Dto...
    default M toModel(D dto) {
        return this.getService().onSave(dto, this.getUser());
    }

    default D getInvalidDto() {
        D dto = this.getDto();
        this.makeInvalid(dto);
        return dto;
    }
    //...Dto

    default void assertEqualSave(M model, D dto) {
        this.assertEqualUpdate(model, dto);
    }

    /**
     * save <count>count<count/> new entities
     *
     * @return saved entities
     */
    @Transactional
    @Override
    default List<M> saveNew(long count, U user) {
        List<M> unsavedModels = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            unsavedModels.add(this.getService().save(this.getDto(), user));
        }
        return unsavedModels;
    }

    /**
     * @return a model that pass <code>validateFunction</code>, saved <code>unsavedDto</code> otherwise.
     */
    default M getOrSave(D unsavedDto, U user, Function<M, Boolean> validateFunction) {
        M model = this.getModel(validateFunction, user);
        if (null == model) {
            return this.getService().save(unsavedDto, user);
        }
        return null;
    }
}
