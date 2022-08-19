package org.bardframework.crud.api.base;


import java.io.Serializable;

public interface BaseModel<I extends Serializable> extends Serializable {

    I getId();

    void setId(I id);
}

