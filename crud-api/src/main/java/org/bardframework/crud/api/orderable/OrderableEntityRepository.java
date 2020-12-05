package org.bardframework.crud.api.orderable;

import org.bardframework.crud.api.base.BaseCriteriaAbstract;
import org.bardframework.crud.api.base.BaseModelAbstract;

public interface OrderableEntityRepository<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, I extends Comparable<? super I>, U> {

    /**
     * @return first record filtered by criteria order by identifier
     */
    M getFirst(C criteria, U user);

    /**
     * @return last record filtered by criteria order by identifier
     */
    M getLast(C criteria, U user);
}
