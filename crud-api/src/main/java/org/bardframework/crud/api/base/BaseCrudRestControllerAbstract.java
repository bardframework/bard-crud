package org.bardframework.crud.api.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Vahid Zafari (v.zafari@chmail.ir) on 1/17/17.
 */

public abstract class BaseCrudRestControllerAbstract<M extends BaseModel<I>, C extends BaseCriteria<I>, D, S extends BaseService<M, C, D, ?, I, U>, I, U> implements ReadRestController<M, C, S, I, U>, WriteRestController<M, D, S, I, U> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final S service;

    protected BaseCrudRestControllerAbstract(S service) {
        this.service = service;
    }

    @Override
    public S getService() {
        return service;
    }

    @Override
    public abstract U getUser();
}
