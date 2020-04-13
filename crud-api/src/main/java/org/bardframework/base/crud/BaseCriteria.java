package org.bardframework.base.crud;

import java.io.Serializable;
import java.util.List;

/**
 * Created by vahid on 3/14/17.
 */
public interface BaseCriteria<I extends Serializable> {

    long getPage();

    long getSize();

    List<I> getExcludes();

    List<I> getIds();
}
