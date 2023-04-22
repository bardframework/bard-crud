package org.bardframework.crud.api.base;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Created by zafari on 4/12/2015.
 */
public interface ReadRestController<M extends BaseModel<I>, C extends BaseCriteria<I>, S extends ReadService<M, C, ?, I, U>, I, U> {

    String GET_URL = "{id}";
    String FILTER_URL = "filter";

    @GetMapping(value = GET_URL)
    default ResponseEntity<M> GET(@PathVariable I id) {
        M result = this.getService().get(id, this.getUser());
        if (null != result) {
            return ResponseEntity.ok().body(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = FILTER_URL)
    default PagedData<M> FILTER(@ModelAttribute C criteria, Pageable page) {
        return this.getService().get(criteria, page, this.getUser());
    }

    S getService();

    U getUser();
}
