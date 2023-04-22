package org.bardframework.crud.api.activable;

public interface ActivableEntityRepository<I, U> {

    /**
     * @return true if operation done
     */
    boolean setEnable(I id, boolean enable, U user);
}
