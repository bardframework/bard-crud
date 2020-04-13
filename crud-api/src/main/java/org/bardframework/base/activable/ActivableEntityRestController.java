package org.bardframework.base.activable;

import org.bardframework.base.crud.BaseModelAbstract;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.io.Serializable;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface ActivableEntityRestController<M extends BaseModelAbstract<I>, S extends ActivableEntityService<M, ?, I, U>, I extends Serializable, U> {

    String ACTIVATE_URL = "{id}/enable";
    String DEACTIVATE_URL = "{id}/disable";

    @PutMapping(value = ACTIVATE_URL, consumes = APPLICATION_JSON_VALUE)
    default M enable(@PathVariable I id) {
        return this.getService().enable(id, this.getUser());
    }

    @PutMapping(value = DEACTIVATE_URL, consumes = APPLICATION_JSON_VALUE)
    default M disable(@PathVariable I id) {
        return this.getService().disable(id, this.getUser());
    }

    S getService();

    U getUser();
}
