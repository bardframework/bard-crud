package org.bardframework.crud.api.base;

import org.bardframework.crud.api.exception.ModelNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Vahid Zafari on 1/17/17.
 */
public interface BaseRepository<M extends BaseModelAbstract<I>, C extends BaseCriteria<I>, I extends Comparable<? super I>, U> {

    Optional<M> get(I id, U user);

    List<M> get(List<I> ids, U user);

    List<M> getAll(U user);

    List<M> get(C criteria, U user);

    List<M> get(C criteria, Sort sort, U user);

    Optional<M> getOne(C criteria, U user);

    long delete(C criteria, U user);

    /**
     * delete using criteria
     *
     * @param id
     * @param user
     * @return count of deleted record
     * @see #delete(BaseCriteria, Object)
     */
    long delete(I id, U user);

    /**
     * delete using criteria
     *
     * @param ids
     * @param user
     * @return count of deleted record
     * @see #delete(BaseCriteria, Object)
     */
    long delete(List<I> ids, U user);

    /**
     * direct delete, not used criteria
     *
     * @param ids
     * @param user
     * @return count of deleted record
     */
    long directDelete(List<I> ids, U user);

    M getEmptyModel();

    C getEmptyCriteria();

    M save(M model, U user);

    List<M> save(List<M> models, U user);

    M update(M model, U user);

    M patch(I id, Map<String, Object> fields, U user) throws ModelNotFoundException;

    Page<M> get(C criteria, Pageable pageable, U user);

    List<I> getIds(C criteria, U user);

    long getCount(C criteria, U user);

    /**
     * @param criteria
     * @return true if any data with given criteria exist, else otherwise.
     */
    boolean isExist(C criteria, U user);

    /**
     * @param criteria
     * @return true if no data with given criteria exist, else otherwise.
     */
    boolean isNotExist(C criteria, U user);
}
