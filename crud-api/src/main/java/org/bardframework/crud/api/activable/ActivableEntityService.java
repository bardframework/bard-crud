package org.bardframework.crud.api.activable;

import org.bardframework.crud.api.base.BaseModel;
import org.bardframework.crud.api.base.BaseRepository;

public interface ActivableEntityService<M extends BaseModel<I>, R extends ActivableEntityRepository<I, U> & BaseRepository<M, ?, I, U>, I extends Comparable<? super I>, U> {

    default M enable(I id, U user) {
        this.getRepository().setEnable(id, true, user);
        return this.getRepository().get(id, user);
    }

    default M disable(I id, U user) {
        this.getRepository().setEnable(id, false, user);
        return this.getRepository().get(id, user);
    }

    R getRepository();
}
