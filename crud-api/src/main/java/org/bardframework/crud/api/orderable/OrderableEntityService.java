package org.bardframework.crud.api.orderable;

import org.bardframework.crud.api.base.BaseCriteriaAbstract;
import org.bardframework.crud.api.base.BaseModelAbstract;

public interface OrderableEntityService<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, R extends OrderableEntityRepository<M, C, I, U>, I extends Comparable<? super I>, U> {

    default M getFirst(C criteria, U user) {
        return this.getRepository().getFirst(criteria, user);
    }

    default M getLast(C criteria, U user) {
        return this.getRepository().getLast(criteria, user);
    }

    R getRepository();
}
