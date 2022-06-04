package org.bardframework.crud.api.base;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Created by zafari on 4/12/2015.
 */
public interface WriteRestController<M extends BaseModel<I>, D, S extends BaseService<M, ?, D, I, U>, I extends Comparable<? super I>, U> {

    String EMPTY_URL = "";
    String ITEM_URL = "{id}";

    @PostMapping(value = EMPTY_URL, consumes = APPLICATION_JSON_VALUE)
    default M SAVE(@RequestBody @Validated(ValidationGroups.Save.class) D dto) {
        return this.getService().save(dto, this.getUser());
    }

    @PutMapping(value = ITEM_URL, consumes = APPLICATION_JSON_VALUE)
    default ResponseEntity<M> UPDATE(@PathVariable I id, @RequestBody @Validated(ValidationGroups.Update.class) D dto) {
        M result = this.getService().update(id, dto, this.getUser());
        if (null != result) {
            return ResponseEntity.ok().body(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping(value = ITEM_URL, consumes = "application/json-patch+json")
    default ResponseEntity<M> PATCH(@PathVariable I id, @RequestBody Map<String, Object> patch) {
        M result = this.getService().patch(id, patch, this.getUser());
        if (null != result) {
            return ResponseEntity.ok().body(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = ITEM_URL)
    default ResponseEntity<Long> DELETE(@PathVariable I id) {
        long result = this.getService().delete(id, this.getUser());
        if (result == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(result);
    }

    S getService();

    U getUser();
}
