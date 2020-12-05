package org.bardframework.crud.api.searchable;

import org.bardframework.crud.api.base.BaseModelAbstract;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchableEntityRepository<M extends BaseModelAbstract<I>, C extends SearchableCriteria, I extends Comparable<? super I>, U> {

    List<M> search(C criteria, Pageable pageable, U user);
}
