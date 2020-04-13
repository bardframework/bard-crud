package org.bardframework.base.crud;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.io.Serializable;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Created by zafari on 4/12/2015.
 */
public interface ReadRestController<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, S extends BaseService<M, C, ?, I, U>, I extends Serializable, U> {

    String GET_URL = "{id}";
    String FILTER_URL = "filter";

    @GetMapping(value = GET_URL)
    default M GET(@PathVariable I id) {
        return this.getService().get(id, this.getUser());
    }

    @PostMapping(value = FILTER_URL, consumes = APPLICATION_JSON_VALUE)
    default DataTableModel<M> FILTER(@RequestBody @Valid C criteria) {
        return this.getService().filter(criteria, this.getUser());
    }

    S getService();

    U getUser();
}
