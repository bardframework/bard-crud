package org.bardframework.crud.api.tree;

import org.bardframework.crud.api.base.BaseModel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public interface TreeEntityRepository<M extends BaseModel<I> & TreeEntityModel<M>, I extends Serializable, U> {

    List<M> getWithChildren(I id, U user);
}
