package org.bardframework.base.crud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

/**
 * Created by Vahid Zafari (v.zafari@chmail.ir) on 1/17/17.
 */

public abstract class BaseCrudRestControllerAbstract<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, D, S extends BaseService<M, C, D, I, U>, I extends Serializable, U> implements ReadRestController<M, C, S, I, U>, WriteRestController<M, D, S, I, U> {
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected S service;

    public S getService() {
        return service;
    }

    public abstract U getUser();
}