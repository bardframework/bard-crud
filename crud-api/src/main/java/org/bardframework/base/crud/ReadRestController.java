package org.bardframework.base.crud;

import org.bardframework.base.util.ResponseUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Created by zafari on 4/12/2015.
 */
public interface ReadRestController<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, S extends BaseService<M, C, ?, I, U>, I extends Comparable<? super I>, U> {

    String GET_LIST_URL = "";
    String GET_URL = "{id}";
    String FILTER_URL = "filter";

    @GetMapping(value = GET_URL)
    default ResponseEntity<M> GET(@PathVariable I id) {
        Optional<M> result = Optional.ofNullable(this.getService().get(id, this.getUser()));
        return ResponseUtil
                .wrapOrNotFound(result);
    }

    @PostMapping(value = FILTER_URL, consumes = APPLICATION_JSON_VALUE)
    default ResponseEntity<Page<M>> FILTER(@RequestBody @Validated C criteria, Pageable page) {
        Page<M> result = this.getService().get(criteria, page, this.getUser());
        return ResponseEntity
                .ok()
                .body(result);
    }

    @GetMapping(value = GET_LIST_URL)
    default ResponseEntity<Page<M>> GET(@Validated C criteria, Pageable page) {
        Page<M> result = this.getService().get(criteria, page, this.getUser());
        return ResponseEntity
                .ok()
                .body(result);
    }

    S getService();

    U getUser();
}