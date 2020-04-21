package org.bardframework.base.searchable;

import org.bardframework.base.crud.BaseModelAbstract;

import java.util.List;

public interface SearchableEntityService<M extends BaseModelAbstract<I>, C extends SearchableCriteria, R extends SearchableEntityRepository<M, C, I, U>, I extends Comparable<? super I>, U> {

    default List<M> search(C criteria, U user) {
        return this.getRepository().search(criteria, user);
    }

    R getRepository();
}