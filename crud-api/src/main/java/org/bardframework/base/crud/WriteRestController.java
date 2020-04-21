package org.bardframework.base.crud;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Created by zafari on 4/12/2015.
 */
public interface WriteRestController<M extends BaseModelAbstract<I>, D, S extends BaseService<M, ?, D, I, U>, I extends Comparable<? super I>, U> {

    String SAVE_URL = "";
    String UPDATE_URL = "{id}";
    String DELETE_URL = "{id}";

    @PostMapping(value = SAVE_URL, consumes = APPLICATION_JSON_VALUE)
    default ResponseEntity<M> SAVE(@RequestBody @Validated(ValidationGroups.Save.class) D dto) throws URISyntaxException {
        M result = this.getService().save(dto, this.getUser());
        return ResponseEntity
                .created(new URI("" + result.getId()))
                .body(result);
    }

    @PutMapping(value = UPDATE_URL, consumes = APPLICATION_JSON_VALUE)
    default ResponseEntity<M> UPDATE(@PathVariable I id, @RequestBody @Validated(ValidationGroups.Update.class) D dto) {
        M result = this.getService().update(id, dto, this.getUser());
        return ResponseEntity
                .ok()
                .body(result);
    }

    @DeleteMapping(value = DELETE_URL)
    default ResponseEntity<Void> DELETE(@PathVariable I id) {
        this.getService().delete(id, this.getUser());
        return ResponseEntity
                .noContent()
                .build();
    }

    S getService();

    U getUser();
}
