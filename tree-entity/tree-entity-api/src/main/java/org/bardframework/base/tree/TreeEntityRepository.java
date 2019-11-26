package org.bardframework.base.tree;

import org.bardframework.base.crud.BaseModelAbstract;

import java.io.Serializable;
import java.util.List;

/**
 * Created by vahid (va.zafari@gmail.com) on 11/12/17.
 */
public interface TreeEntityRepository<M extends BaseModelAbstract<I> & TreeEntityModel<M>, I extends Serializable, U> {

    List<M> getWithChildren(I id, U user);
}
