package org.bardframework.crud.api.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.bardframework.crud.api.exception.ModelNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/6/17.
 */
public interface BaseService<M extends BaseModelAbstract<I>, C extends BaseCriteria<I>, D, I extends Comparable<? super I>, U> {
    Optional<M> get(I id, U user);

    long delete(I id, U user);

    M save(D dto, U user) throws ModelNotFoundException;

    M update(I id, D dto, U user) throws ModelNotFoundException;

    M patch(I id, JsonPatch patch, U user) throws JsonPatchException, JsonProcessingException, ModelNotFoundException;

    Page<M> get(C criteria, Pageable pageable, U user);
}
