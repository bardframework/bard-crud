package org.bardframework.crud.api.base;

/**
 *
 */
public abstract class BaseModelAbstract<I> implements BaseModel<I> {

    protected I id;

    public BaseModelAbstract() {
    }

    public BaseModelAbstract(I id) {
        this.id = id;
    }

    @Override
    public I getId() {
        return id;
    }

    public void setId(I id) {
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseModelAbstract<?> baseModel = (BaseModelAbstract<?>) o;
        if (id != null) {
            return id.equals(baseModel.id);
        }
        return baseModel.id == null;
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return 0;
    }
}

