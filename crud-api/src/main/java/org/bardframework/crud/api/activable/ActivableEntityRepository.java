package org.bardframework.crud.api.activable;

import java.io.Serializable;

public interface ActivableEntityRepository<I extends Serializable, U> {

    /**
     * @return true if operation done
     */
    boolean setEnable(I id, boolean enable, U user);
}
