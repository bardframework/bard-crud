package org.bardframework.crud.api.base;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

/**
 * Created by Vahid Zafari on 1/17/17.
 */
public interface BaseRepository<M extends BaseModel<I>, C extends BaseCriteria<I>, I extends Comparable<? super I>, U> {

    M get(I id, U user);

    List<M> get(List<I> ids, U user);

    List<M> get(C criteria, U user);

    List<M> get(C criteria, Sort sort, U user);

    M getOne(C criteria, U user);

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
    long delete(List<I> ids, U user);

    /**
     * direct delete, not used criteria
     *
     * @return count of deleted record
     */
    long directDelete(List<I> ids, U user);


    M save(M model, U user);

    List<M> save(List<M> models, U user);

    M update(M model, U user);

    M patch(I id, Map<String, Object> fields, U user);

    PagedData<M> get(C criteria, Pageable pageable, U user);

    List<I> getIds(C criteria, U user);

    long getCount(C criteria, U user);

    /**
     * @return true if any data with given criteria exist, else otherwise.
     */
    boolean isExist(C criteria, U user);

    /**
     * @return true if no data with given criteria exist, else otherwise.
     */
    boolean isNotExist(C criteria, U user);
}
