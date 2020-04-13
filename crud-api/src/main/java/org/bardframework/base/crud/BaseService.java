package org.bardframework.base.crud;

import java.io.Serializable;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/6/17.
 */
public interface BaseService<M extends BaseModelAbstract<I>, C extends BaseCriteria<I>, D, I extends Serializable, U> {
    M get(I id, U user);

    long delete(I id, U user);

    M save(D dto, U user);

    M update(I id, D dto, U user);

    DataTableModel<M> filter(C criteria, U user);
}
