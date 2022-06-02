package org.bardframework.crud.api.orderable;

import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModel;

public interface OrderableEntityRepository<M extends BaseModel<I>, C extends BaseCriteria<I>, I extends Comparable<? super I>, U> {

    /**
     * @return first record filtered by criteria order by identifier
     */
    M getFirst(C criteria, U user);

    /**
     * @return last record filtered by criteria order by identifier
     */
    M getLast(C criteria, U user);
}
