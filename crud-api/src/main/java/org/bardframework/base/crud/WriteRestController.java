package org.bardframework.base.crud;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Created by zafari on 4/12/2015.
 */
public interface WriteRestController<M extends BaseModelAbstract<I>, D, S extends BaseService<M, ?, D, I, U>, I extends Serializable, U> {

    String SAVE_URL = "";
    String UPDATE_URL = "{id}";
    String DELETE_URL = "{id}";

    @PostMapping(value = SAVE_URL, consumes = APPLICATION_JSON_VALUE)
    default M SAVE(@RequestBody @Validated(ValidationGroups.Save.class) D dto) {
        return this.getService().save(dto, this.getUser());
    }

    @PutMapping(value = UPDATE_URL, consumes = APPLICATION_JSON_VALUE)
    default M UPDATE(@PathVariable I id, @RequestBody @Validated(ValidationGroups.Update.class) D dto) {
        return this.getService().update(id, dto, this.getUser());
    }

    @DeleteMapping(value = DELETE_URL)
    default Long DELETE(@PathVariable I id) {
        return this.getService().delete(id, this.getUser());
    }

    S getService();

    U getUser();
}
