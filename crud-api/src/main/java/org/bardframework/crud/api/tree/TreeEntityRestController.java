package org.bardframework.crud.api.tree;

import org.bardframework.crud.api.base.BaseCriteria;
import org.bardframework.crud.api.base.BaseModelAbstract;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public interface TreeEntityRestController<M extends BaseModelAbstract<I> & TreeEntityModel<M>, C extends BaseCriteria<I> & TreeEntityCriteria<I>, S extends TreeEntityService<M, C, ?, I, U>, I extends Comparable<? super I>, U> {
    String TREE_URL = "tree";

    @GetMapping(value = TREE_URL)
    default M getTree() {
        return this.getService().getTree(this.getUser());
    }

    @GetMapping(value = "{rootId}/" + TREE_URL)
    default M getTree(@PathVariable I rootId) {
        return this.getService().getTree(rootId, this.getUser());
    }

    S getService();

    U getUser();
}
