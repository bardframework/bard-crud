package org.bardframework.base.orderable;

import org.bardframework.base.crud.BaseCriteriaAbstract;
import org.bardframework.base.crud.BaseModelAbstract;

import java.io.Serializable;

public interface OrderableEntityService<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, R extends OrderableEntityRepository<M, C, I, U>, I extends Serializable, U> {

    default M getFirst(C criteria, U user) {
        return this.getRepository().getFirst(criteria, user);
    }

    default M getLast(C criteria, U user) {
        return this.getRepository().getLast(criteria, user);
    }

    R getRepository();
}