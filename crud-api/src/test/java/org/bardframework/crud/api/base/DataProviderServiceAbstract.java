package org.bardframework.crud.api.base;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Sama-PC on 08/05/2017.
 */
public abstract class DataProviderServiceAbstract<M extends BaseModel<I>, C extends BaseCriteria<I>, D, S extends BaseServiceAbstract<M, C, D, R, I, U>, R extends BaseRepository<M, C, I, U>, I extends Comparable<? super I>, U> extends DataProviderRepositoryAbstract<M, C, R, I, U> {

    protected final S service;

    public DataProviderServiceAbstract(S service) {
        super(service.getRepository());
        this.service = service;
    }

    @Override
    public final M getUnsavedModel(U user) {
        return this.toModel(this.getDto(user), user);
    }

    public final List<D> getUnsavedDtos(long count, U user) {
        List<D> models = new ArrayList<>();
        while (models.size() < count) {
            D dto = this.getDto(user);
            if (models.stream().noneMatch(element -> this.isDuplicate(element, dto, user))) {
                models.add(dto);
            }
        }
        return models;
    }

    /**
     * use {@link #isDuplicate(M, M, U)} in default implementation
     */
    protected boolean isDuplicate(D first, D second, U user) {
        return this.isDuplicate(this.toModel(first, user), this.toModel(second, user), user);
    }

    @Override
    protected boolean isDuplicate(M first, M second, U user) {
        return super.isDuplicate(first, second, user);
    }

    //Dto...
    protected M toModel(D dto, U user) {
        return service.onSave(dto, user);
    }

    public abstract D getDto(U user);

    public final D getInvalidDto(U user) {
        return this.makeInvalid(this.getDto(user));
    }

    public final D getUnsavedInvalidDto(U user) {
        return makeInvalid(this.getDto(user));
    }
    //...Dto

    public void assertEqualSave(M model, D dto) {
        this.assertEqualUpdate(model, dto);
    }

    public abstract void assertEqualUpdate(M model, D dto);

    /**
     * save <count>count<count/> new entities
     *
     * @return saved entities
     */
    @Transactional
    @Override
    public List<M> saveNew(long count, U user) {
        List<M> unsavedModels = new ArrayList<>();
        for (long i = 0; i < count; i++) {
            unsavedModels.add(service.save(this.getDto(user), user));
        }
        return unsavedModels;
    }

    /**
     * @return a model that pass <code>validateFunction</code>, saved <code>unsavedDto</code> otherwise.
     */
    public M getOrSave(D unsavedDto, U user, Function<M, Boolean> validateFunction) {
        M model = this.getModel(validateFunction, user);
        if (null == model) {
            return service.save(unsavedDto, user);
        }
        return null;
    }

    protected abstract D makeInvalid(D dto);
}
