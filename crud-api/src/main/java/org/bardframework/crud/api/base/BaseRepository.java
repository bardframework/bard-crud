package org.bardframework.crud.api.base;

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

    M getEmptyModel();

    C getEmptyCriteria();

    M save(M model, U user);

    List<M> save(List<M> models, U user);

    M update(M model, U user);

    Optional<M> patch(I id, Map<String, Object> fields, U user);

    Page<M> get(C criteria, Pageable pageable, U user);

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
