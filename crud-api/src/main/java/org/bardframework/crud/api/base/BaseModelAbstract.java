package org.bardframework.crud.api.base;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 *
 */
public abstract class BaseModelAbstract<I extends Comparable<? super I>> implements Serializable {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    protected I id;

    public BaseModelAbstract() {
    }

    public BaseModelAbstract(I id) {
        this.id = id;
    }

    public I getId() {
        return id;
    }

    public final void setId(I id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseModelAbstract baseModel = (BaseModelAbstract) o;
        return id != null ? id.equals(baseModel.id) : baseModel.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

