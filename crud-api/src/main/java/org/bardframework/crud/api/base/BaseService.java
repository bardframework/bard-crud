package org.bardframework.crud.api.base;

import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/6/17.
 */
public interface BaseService<M extends BaseModel<I>, C extends BaseCriteria<I>, D, I extends Comparable<? super I>, U> {
    M get(I id, U user);

    long delete(I id, U user);

    M save(D dto, U user);

    M update(I id, D dto, U user);

    M patch(I id, Map<String, Object> patch, U user);

    PagedData<M> get(C criteria, Pageable pageable, U user);
}
