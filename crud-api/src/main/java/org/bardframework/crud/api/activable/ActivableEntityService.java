package org.bardframework.crud.api.activable;

import org.bardframework.crud.api.base.BaseModelAbstract;
import org.bardframework.crud.api.base.BaseRepository;

import java.util.Optional;

public interface ActivableEntityService<M extends BaseModelAbstract<I>, R extends ActivableEntityRepository<I, U> & BaseRepository<M, ?, I, U>, I extends Comparable<? super I>, U> {

    default Optional<M> enable(I id, U user) {
        this.getRepository().setEnable(id, true, user);
        return this.getRepository().get(id, user);
    }

    default Optional<M> disable(I id, U user) {
        this.getRepository().setEnable(id, false, user);
        return this.getRepository().get(id, user);
    }

    R getRepository();
}
