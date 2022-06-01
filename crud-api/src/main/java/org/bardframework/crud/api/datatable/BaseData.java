package org.bardframework.crud.api.datatable;

/**
 * @author v.zafari
 */
public class BaseData<T extends BaseData<T>> implements Comparable<T> {

    protected String id;
    protected String name;
    protected int sequence;

    /**
     * for json deserializer
     */
    public BaseData() {
        super();
    }

    public BaseData(String id) {
        this();
        this.id = id;
    }


    public BaseData(String id, String name) {
        this(id);
        this.name = name;
    }

    public BaseData(String id, String name, int sequence) {
        this(id, name);
        this.sequence = sequence;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseData baseData = (BaseData) o;

        return id.equals(baseData.id);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + "'" +
                "} " + super.toString();
    }

    @Override
    public int compareTo(T other) {
        if (null == other) {
            return 1;
        }
        if (this.getSequence() == other.getSequence()) {
            return this.getName().compareTo(other.getName());
        }
        return Integer.compare(this.getSequence(), other.getSequence());
    }
}
