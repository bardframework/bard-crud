package org.bardframework.base.searchable;

import org.bardframework.base.crud.BaseModelAbstract;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface SearchableEntityRestController<M extends BaseModelAbstract<I>, C extends SearchableCriteria, S extends SearchableEntityService<M, C, ?, I, U>, I extends Serializable, U> {

    String SEARCH_URL = "search";

    @PostMapping(value = SEARCH_URL, consumes = APPLICATION_JSON_VALUE)
    default List<M> SEARCH(@RequestBody @Validated C criteria) {
        return this.getService().search(criteria, this.getUser());
    }

    S getService();

    U getUser();
}
