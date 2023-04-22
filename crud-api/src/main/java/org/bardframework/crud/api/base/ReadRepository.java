package org.bardframework.crud.api.base;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.List;

/**
 * Created by Vahid Zafari on 1/17/17.
 */
public interface ReadRepository<M extends BaseModel<I>, C extends BaseCriteria<I>, I, U> {

    M get(I id, U user);

    List<M> get(Collection<I> ids, U user);

    List<M> get(C criteria, U user);

    List<M> get(C criteria, Sort sort, U user);

    M getOne(C criteria, U user);

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
