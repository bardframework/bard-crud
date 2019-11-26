package org.bardframework.base.activable;

import java.io.Serializable;

public interface ActivableEntityRepository<I extends Serializable, U> {

    /**
     * @param id
     * @param enable
     * @param user
     * @return true if operation done
     */
    boolean setEnable(I id, boolean enable, U user);
}
