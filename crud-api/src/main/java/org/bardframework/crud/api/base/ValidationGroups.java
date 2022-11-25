package org.bardframework.crud.api.base;

import jakarta.validation.groups.Default;

/**
 * Created by vahid on 5/13/17.
 */
public interface ValidationGroups {

    interface Save extends Default {
    }

    interface Update extends Default {
    }
}
