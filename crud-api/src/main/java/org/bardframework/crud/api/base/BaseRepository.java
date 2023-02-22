package org.bardframework.crud.api.base;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Vahid Zafari on 1/17/17.
 */
public interface BaseRepository<M extends BaseModel<I>, C extends BaseCriteria<I>, I extends Serializable, U> extends ReadRepository<M, C, I, U> {

    long delete(C criteria, U user);

    /**
     * delete using criteria
     *
     * @return count of deleted record
     * @see #delete(BaseCriteria, Object)
     */
    long delete(I id, U user);

    /**
     * delete using criteria
     *
     * @return count of deleted record
     * @see #delete(BaseCriteria, Object)
     */
    long delete(Collection<I> ids, U user);

    M save(M model, U user);

    List<M> save(Collection<M> models, U user);

    M update(M model, U user);

    List<M> update(Collection<M> models, U user);

    M patch(I id, Map<String, Object> fields, U user);
}