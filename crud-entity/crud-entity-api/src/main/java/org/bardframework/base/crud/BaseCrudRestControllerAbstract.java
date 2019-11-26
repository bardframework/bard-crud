package org.bardframework.base.crud;

import org.bardframework.commons.web.base.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

/**
 * Created by Vahid Zafari (v.zafari@chmail.ir) on 1/17/17.
 */

public abstract class BaseCrudRestControllerAbstract<M extends BaseModelAbstract<I>, C extends BaseCriteriaAbstract<I>, D, S extends BaseService<M, C, D, I, U>, I extends Serializable, U> extends BaseRestController<U> implements ReadRestController<M, C, S, I, U>, WriteRestController<M, D, S, I, U> {

    @Autowired
    protected S service;

    public S getService() {
        return service;
    }
}