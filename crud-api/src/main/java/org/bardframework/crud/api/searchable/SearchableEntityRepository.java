package org.bardframework.crud.api.searchable;

import org.bardframework.crud.api.base.BaseModel;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchableEntityRepository<M extends BaseModel<I>, C extends SearchableEntityCriteria, I, U> {

    List<M> search(C criteria, Pageable pageable, U user);
}
