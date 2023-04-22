package org.bardframework.crud.api.searchable;

import org.bardframework.crud.api.base.BaseModel;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchableEntityService<M extends BaseModel<I>, C extends SearchableEntityCriteria, R extends SearchableEntityRepository<M, C, I, U>, I, U> {

    default List<M> search(C criteria, Pageable pageable, U user) {
        return this.getRepository().search(criteria, pageable, user);
    }

    R getRepository();
}
