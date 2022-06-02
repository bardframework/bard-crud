package org.bardframework.crud.api.base;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Created by zafari on 4/12/2015.
 */
public interface ReadRestController<M extends BaseModel<I>, C extends BaseCriteriaAbstract<I>, S extends BaseService<M, C, ?, I, U>, I extends Comparable<? super I>, U> {

    String GET_LIST_URL = "";
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

    @PostMapping(value = FILTER_URL, consumes = APPLICATION_JSON_VALUE)
    default ResponseEntity<PagedData<M>> FILTER(@RequestBody @Validated C criteria, Pageable page) {
        PagedData<M> result = this.getService().get(criteria, page, this.getUser());
        return ResponseEntity.ok().body(result);
    }

    @GetMapping(value = GET_LIST_URL)
    default ResponseEntity<PagedData<M>> GET(@Validated C criteria, Pageable page) {
        PagedData<M> result = this.getService().get(criteria, page, this.getUser());
        return ResponseEntity.ok().body(result);
    }

    S getService();

    U getUser();
}
