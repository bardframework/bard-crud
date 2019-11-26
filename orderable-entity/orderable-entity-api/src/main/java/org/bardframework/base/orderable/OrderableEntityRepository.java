package org.bardframework.base.orderable;

import org.bardframework.base.crud.BaseCriteriaAbstract;
import org.bardframework.base.crud.BaseModelAbstract;

import java.io.Serializable;

public interface OrderableEntityRepository<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, I extends Serializable, U> {

    /**
     * @param criteria
     * @return first record filtered by criteria order by identifier
     */
    M getFirst(C criteria, U user);

    /**
     * @param criteria
     * @return last record filtered by criteria order by identifier
     */
    M getLast(C criteria, U user);
}
