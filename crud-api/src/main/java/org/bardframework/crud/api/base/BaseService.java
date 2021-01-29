package org.bardframework.crud.api.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
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

    M patch(I id, JsonPatch patch, U user) throws JsonPatchException, JsonProcessingException;

    Page<M> get(C criteria, Pageable pageable, U user);
}
