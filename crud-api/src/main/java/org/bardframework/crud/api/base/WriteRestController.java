package org.bardframework.crud.api.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Created by zafari on 4/12/2015.
 */
public interface WriteRestController<M extends BaseModelAbstract<I>, D, S extends BaseService<M, ?, D, I, U>, I extends Comparable<? super I>, U> {

    String EMPTY_URL = "";
    String ITEM_URL = "{id}";

    @PostMapping(value = EMPTY_URL, consumes = APPLICATION_JSON_VALUE)
    default ResponseEntity<M> SAVE(@RequestBody @Validated(ValidationGroups.Save.class) D dto) throws URISyntaxException {
        Optional<M> result = this.getService().save(dto, this.getUser());
        return result.isPresent() ? ResponseEntity.created(new URI("" + result.get().getId())).body(result.get()) : ResponseEntity.notFound().build();
    }

    @PutMapping(value = ITEM_URL, consumes = APPLICATION_JSON_VALUE)
    default ResponseEntity<M> UPDATE(@PathVariable I id, @RequestBody @Validated(ValidationGroups.Update.class) D dto) {
        Optional<M> result = this.getService().update(id, dto, this.getUser());
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping(value = ITEM_URL, consumes = "application/json-patch+json")
    default ResponseEntity<M> PATCH(@PathVariable I id, @RequestBody JsonPatch patch) throws JsonPatchException, JsonProcessingException {
        Optional<M> result = this.getService().patch(id, patch, this.getUser());
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = ITEM_URL)
    default ResponseEntity<Void> DELETE(@PathVariable I id) {
        long result = this.getService().delete(id, this.getUser());
        return result == 0 ? ResponseEntity.notFound().build() : ResponseEntity.noContent().build();
    }

    S getService();

    U getUser();
}