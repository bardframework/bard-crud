package org.bardframework.base.searchable;

import org.bardframework.base.crud.BaseModelAbstract;

import java.util.List;

public interface SearchableEntityRepository<M extends BaseModelAbstract<I>, C extends SearchableCriteria, I extends Comparable<? super I>, U> {

    List<M> search(C criteria, U user);
}
