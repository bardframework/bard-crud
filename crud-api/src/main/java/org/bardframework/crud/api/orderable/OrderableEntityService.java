package org.bardframework.crud.api.orderable;

import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;

import java.io.Serializable;

public interface OrderableEntityService<M extends BaseModel<I>, C extends BaseCriteria<I>, R extends OrderableEntityRepository<M, C, I, U>, I extends Serializable, U> {

    default M getFirst(C criteria, U user) {
        return this.getRepository().getFirst(criteria, user);
    }

    default M getLast(C criteria, U user) {
        return this.getRepository().getLast(criteria, user);
    }

    R getRepository();
}
