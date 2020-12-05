package org.bardframework.crud.api.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/6/17.
 */
public interface BaseService<M extends BaseModelAbstract<I>, C extends BaseCriteria<I>, D, I extends Comparable<? super I>, U> {
    M get(I id, U user);

    long delete(I id, U user);

    M save(D dto, U user);

    M update(I id, D dto, U user);

    Page<M> get(C criteria, Pageable pageable, U user);
}
