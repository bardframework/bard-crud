package org.bardframework.crud.api.event;

import org.bardframework.crud.api.base.BaseModelAbstract;

import java.util.List;

public interface ModelEventProducer<M extends BaseModelAbstract<?>, I extends Comparable<? super I>, U> {
    void onUpdate(M pre, M model, U user);

    void onDelete(List<M> pre, U user);

    void onSave(List<M> model, U user);
}