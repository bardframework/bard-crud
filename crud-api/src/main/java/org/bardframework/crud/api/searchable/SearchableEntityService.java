package org.bardframework.crud.api.searchable;

import org.bardframework.crud.api.base.BaseModel;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

public interface SearchableEntityService<M extends BaseModel<I>, C extends SearchableCriteria, R extends SearchableEntityRepository<M, C, I, U>, I extends Serializable, U> {

    default List<M> search(C criteria, Pageable pageable, U user) {
        return this.getRepository().search(criteria, pageable, user);
    }

    R getRepository();
}
